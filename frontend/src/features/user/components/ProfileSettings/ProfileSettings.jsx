import { useRef, useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";

import Card from "../../../../components/ui/Card";
import Input from "../../../../components/ui/Input";
import Button from "../../../../components/ui/Button";
import FormMessage from "../../../../components/ui/FormMessage";

import { useAuth } from "../../../../hooks/useAuth";
import { updateProfile, updateAvatar } from "../../services/userService";
import { ROUTES } from "../../../../constants/routes";
import "./ProfileSettings.css";

const profileSchema = z.object({
  fullName: z.string().min(1, "Họ và tên không được để trống"),
  email: z.string().email("Email không hợp lệ").optional(),
});

/**
 * Giới hạn trùng khớp với API: JPEG/PNG/WebP, tối đa 5MB.
 */
const AVATAR_ALLOWED_TYPES = ["image/jpeg", "image/png", "image/webp"];
const AVATAR_ALLOWED_EXTENSIONS = /\.(jpe?g|png|webp)$/i;
const AVATAR_MAX_SIZE_BYTES = 5 * 1024 * 1024;
const AVATAR_HINT = "Chấp nhận ảnh JPEG, PNG hoặc WebP, dung lượng tối đa 5MB.";

function ProfileSettings() {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();

  const fileInputRef = useRef(null);
  const [submitError, setSubmitError] = useState("");
  const [submitSuccess, setSubmitSuccess] = useState("");
  const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);
  const [avatarMessage, setAvatarMessage] = useState(null);
  const [avatarUrl, setAvatarUrl] = useState(user?.avatar || "");

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      fullName: user?.fullName || "",
      email: user?.email || "",
    },
  });

  // Load user data into form when user is loaded
  useEffect(() => {
    if (user) {
      setValue("fullName", user.fullName || "");
      setValue("email", user.email || "");
      setAvatarUrl(user.avatar || "");
    }
  }, [user, setValue]);

  const onSubmit = async (data) => {
    setSubmitError("");
    setSubmitSuccess("");

    try {
      /**
       * API chỉ cho phép cập nhật họ tên,
       * email là định danh tài khoản nên không thay đổi được.
       */
      const response = await updateProfile({ fullName: data.fullName });

      // Update global user state
      if (response && response.data) {
        updateUser({
          fullName: response.data.fullName ?? data.fullName,
          avatar: response.data.avatar ?? avatarUrl,
        });
      } else {
        updateUser({ fullName: data.fullName });
      }

      setSubmitSuccess("Thay đổi thông tin thành công!");

      // Auto-hide after 3 seconds
      setTimeout(() => {
        setSubmitSuccess("");
      }, 3000);
    } catch (error) {
      const errorMessage =
        error.response?.data?.message ||
        "Đã xảy ra lỗi khi cập nhật thông tin";
      setSubmitError(errorMessage);

      // Auto-hide error after 3 seconds
      setTimeout(() => {
        setSubmitError("");
      }, 3000);
    }
  };

  const handleEditAvatarClick = () => {
    fileInputRef.current?.click();
  };

  const handleAvatarChange = async (event) => {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    setAvatarMessage(null);

    /**
     * Kiểm tra loại file và dung lượng trước khi tải lên.
     */
    const isAllowedType =
      AVATAR_ALLOWED_TYPES.includes(file.type) ||
      AVATAR_ALLOWED_EXTENSIONS.test(file.name);
    const isWithinSize = file.size > 0 && file.size <= AVATAR_MAX_SIZE_BYTES;

    if (!isAllowedType || !isWithinSize) {
      setAvatarMessage({
        type: "error",
        text: !isAllowedType
          ? "Chỉ chấp nhận ảnh JPEG, PNG hoặc WebP."
          : "Kích thước ảnh không được vượt quá 5MB.",
      });

      // Cho phép chọn lại cùng một file
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
      return;
    }

    setIsUploadingAvatar(true);

    try {
      const response = await updateAvatar(file);

      if (response?.data) {
        updateUser({
          fullName: response.data.fullName ?? user?.fullName,
          avatar: response.data.avatar,
        });
        setAvatarUrl(response.data.avatar);
      }

      setAvatarMessage({
        type: "success",
        text: "Cập nhật ảnh đại diện thành công!",
      });
      setTimeout(() => setAvatarMessage(null), 3000);
    } catch (error) {
      const errorMessage =
        error.response?.data?.message ||
        "Đã xảy ra lỗi khi cập nhật ảnh đại diện";
      setAvatarMessage({ type: "error", text: errorMessage });
      setTimeout(() => setAvatarMessage(null), 3000);
    } finally {
      setIsUploadingAvatar(false);

      // Cho phép chọn lại cùng một file
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const handleCancel = () => {
    navigate(ROUTES.HOME);
  };

  return (
    <div className="profile-settings-wrapper">
      <Card className="profile-settings-card">
        <h2 className="profile-title">Thay đổi thông tin cá nhân</h2>

        <div className="profile-divider"></div>

        <form
          onSubmit={handleSubmit(onSubmit)}
          onChange={() => {
            setSubmitError("");
            setSubmitSuccess("");
          }}
          noValidate
        >
          {/* Avatar Section */}
          <div className="avatar-section">
            <div className="avatar-circle">
              {avatarUrl ? (
                <img src={avatarUrl} alt="Avatar" className="avatar-image" />
              ) : (
                <div className="avatar-placeholder"></div>
              )}
            </div>
            <button
              type="button"
              className="btn-edit-avatar"
              onClick={handleEditAvatarClick}
              disabled={isUploadingAvatar}
              aria-busy={isUploadingAvatar}
            >
              {isUploadingAvatar ? "Đang tải ảnh..." : "Chỉnh sửa ảnh đại diện"}
            </button>
            <p className="avatar-hint">{AVATAR_HINT}</p>
            {avatarMessage && (
              <p
                className={`avatar-message ${
                  avatarMessage.type === "error" ? "is-error" : "is-success"
                }`}
                role="alert"
              >
                {avatarMessage.text}
              </p>
            )}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              hidden
              onChange={handleAvatarChange}
            />
          </div>

          {/* Form Fields */}
          <div className="form-fields-container">
            <div className="form-group row">
              <label className="field-label">Họ và tên</label>
              <div className="field-input">
                <Input
                  type="text"
                  placeholder="Nhập họ và tên"
                  error={errors.fullName?.message}
                  {...register("fullName")}
                />
              </div>
            </div>

            <div className="form-group row">
              <label className="field-label">Email</label>
              <div className="field-input">
                <Input type="email" disabled {...register("email")} />
              </div>
            </div>

            <div className="form-group row">
              <label className="field-label">Mật khẩu</label>
              <div className="field-input password-link-container">
                <Link to={ROUTES.CHANGE_PASSWORD} className="btn-change-password">
                  Đổi mật khẩu
                </Link>
              </div>
            </div>
          </div>

          {submitError && (
            <FormMessage variant="error">{submitError}</FormMessage>
          )}
          {submitSuccess && (
            <FormMessage variant="success">{submitSuccess}</FormMessage>
          )}

          {/* Buttons */}
          <div className="form-actions">
            <Button
              type="submit"
              disabled={isSubmitting}
              aria-busy={isSubmitting}
            >
              {isSubmitting ? "Đang lưu..." : "Lưu thay đổi"}
            </Button>
            <button type="button" className="btn-cancel" onClick={handleCancel}>
              Huỷ
            </button>
          </div>
        </form>
      </Card>
    </div>
  );
}

export default ProfileSettings;
