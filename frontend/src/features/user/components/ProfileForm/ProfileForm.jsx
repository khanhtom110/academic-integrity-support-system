import { useRef, useState } from "react";
import { Link, useNavigate, useOutletContext } from "react-router-dom";

import Button from "../../../../components/ui/Button";
import FormMessage from "../../../../components/ui/FormMessage";
import { CameraIcon, TrashIcon } from "../../../../components/ui/Icons";
import { ROUTES } from "../../../../constants/routes";
import { deleteAvatar, updateProfile, uploadAvatar } from "../../services/userService";
import { AVATAR_ACCEPT, getUserError, validateAvatar } from "../../utils/profile";
import UserAvatar from "../UserAvatar/UserAvatar";

import "./ProfileForm.css";

function ProfileForm() {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const { profile, setProfile } = useOutletContext();
  const [fullName, setFullName] = useState(profile.fullName || "");
  const [nameError, setNameError] = useState("");
  const [feedback, setFeedback] = useState(null);
  const [isSaving, setIsSaving] = useState(false);
  const [avatarAction, setAvatarAction] = useState("");
  const normalizedName = fullName.trim();
  const savedName = (profile.fullName || "").trim();
  const isAvatarBusy = Boolean(avatarAction);
  const isUnchanged = normalizedName === savedName;

  const applyProfileResponse = (response) => {
    if (!response?.data) throw new Error("Phản hồi hồ sơ không hợp lệ.");
    setProfile(response.data);
    return response.data;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFeedback(null);

    if (!normalizedName) {
      setNameError("Vui lòng nhập họ và tên.");
      return;
    }

    setNameError("");
    setIsSaving(true);

    try {
      const response = await updateProfile({ fullName: normalizedName });
      const updatedProfile = applyProfileResponse(response);
      setFullName(updatedProfile.fullName || normalizedName);
      setFeedback({ type: "success", message: "Thông tin của bạn đã được lưu thành công." });
    } catch (error) {
      setFeedback({
        type: "error",
        message: getUserError(error, "Không thể cập nhật thông tin. Vui lòng thử lại."),
      });
    } finally {
      setIsSaving(false);
    }
  };

  const handleAvatarChange = async (event) => {
    const [file] = event.target.files || [];
    event.target.value = "";
    setFeedback(null);

    const validationError = validateAvatar(file);
    if (validationError) {
      setFeedback({ type: "error", message: validationError });
      return;
    }

    setAvatarAction("upload");

    try {
      const response = await uploadAvatar(file);
      applyProfileResponse(response);
      setFeedback({ type: "success", message: "Ảnh đại diện đã được cập nhật." });
    } catch (error) {
      setFeedback({
        type: "error",
        message: getUserError(error, "Không thể cập nhật ảnh đại diện. Vui lòng thử lại."),
      });
    } finally {
      setAvatarAction("");
    }
  };

  const handleDeleteAvatar = async () => {
    const confirmed = window.confirm("Bạn có chắc muốn xóa ảnh đại diện hiện tại?");
    if (!confirmed) return;

    setFeedback(null);
    setAvatarAction("delete");

    try {
      const response = await deleteAvatar();
      applyProfileResponse(response);
      setFeedback({ type: "success", message: "Ảnh đại diện đã được xóa." });
    } catch (error) {
      setFeedback({
        type: "error",
        message: getUserError(error, "Không thể xóa ảnh đại diện. Vui lòng thử lại."),
      });
    } finally {
      setAvatarAction("");
    }
  };

  const handleCancel = () => {
    setFullName(profile.fullName || "");
    setNameError("");
    setFeedback(null);
    navigate(ROUTES.HOME);
  };

  return (
    <>
      {feedback && (
        <div className="profile-toast">
          <FormMessage variant={feedback.type}>{feedback.message}</FormMessage>
        </div>
      )}

      <section className="profile-card" aria-labelledby="profile-title">
        <h1 id="profile-title">Thay đổi thông tin cá nhân</h1>

        <form onSubmit={handleSubmit} noValidate>
          <div className="profile-avatar-section">
            <UserAvatar avatar={profile.avatar} fullName={profile.fullName} size="large" />
            <input
              ref={fileInputRef}
              type="file"
              className="visually-hidden"
              accept={AVATAR_ACCEPT}
              onChange={handleAvatarChange}
              tabIndex={-1}
            />
            <button
              type="button"
              className="profile-avatar-action"
              onClick={() => fileInputRef.current?.click()}
              disabled={isAvatarBusy}
              aria-busy={avatarAction === "upload"}
            >
              <CameraIcon size={19} />
              {avatarAction === "upload" ? "Đang tải ảnh..." : "Chỉnh sửa ảnh đại diện"}
            </button>
            {profile.avatar && (
              <button
                type="button"
                className="profile-avatar-delete"
                onClick={handleDeleteAvatar}
                disabled={isAvatarBusy}
                aria-busy={avatarAction === "delete"}
              >
                <TrashIcon size={17} />
                {avatarAction === "delete" ? "Đang xóa..." : "Xóa ảnh"}
              </button>
            )}
          </div>

          <div className="profile-fields">
            <div className="profile-field">
              <label htmlFor="profile-full-name">Họ và tên</label>
              <div>
                <input
                  id="profile-full-name"
                  value={fullName}
                  autoComplete="name"
                  onChange={(event) => {
                    setFullName(event.target.value);
                    if (nameError) setNameError("");
                    if (feedback) setFeedback(null);
                  }}
                  aria-invalid={nameError ? "true" : undefined}
                  aria-describedby={nameError ? "profile-full-name-error" : undefined}
                />
                {nameError && <p id="profile-full-name-error" className="profile-field-error" role="alert">{nameError}</p>}
              </div>
            </div>

            <div className="profile-field">
              <label htmlFor="profile-email">Email</label>
              <input id="profile-email" type="email" value={profile.email || ""} disabled />
            </div>

            <div className="profile-field profile-password-row">
              <span>Mật khẩu</span>
              <Link to={ROUTES.CHANGE_PASSWORD}>
                Đổi mật khẩu
              </Link>
            </div>
          </div>

          <div className="profile-actions">
            <Button type="submit" disabled={isSaving || isUnchanged || isAvatarBusy} aria-busy={isSaving}>
              {isSaving ? "Đang lưu..." : "Lưu thay đổi"}
            </Button>
            <button type="button" className="profile-cancel" onClick={handleCancel} disabled={isSaving || isAvatarBusy}>
              Hủy
            </button>
          </div>
        </form>
      </section>
    </>
  );
}

export default ProfileForm;
