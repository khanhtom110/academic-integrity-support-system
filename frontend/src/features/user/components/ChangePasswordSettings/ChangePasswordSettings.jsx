import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";

import Card from "../../../../components/ui/Card";
import PasswordInput from "../../../../components/ui/PasswordInput";
import Button from "../../../../components/ui/Button";
import FormMessage from "../../../../components/ui/FormMessage";

import { useAuth } from "../../../../hooks/useAuth";
import { changePassword } from "../../services/userService";
import { ROUTES } from "../../../../constants/routes";
import "./ChangePasswordSettings.css";

const passwordSchema = z.object({
  oldPassword: z.string().min(1, "Vui lòng nhập mật khẩu hiện tại"),
  newPassword: z.string()
    .min(6, "Mật khẩu nên đặt từ 6 kí tự trở lên")
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_]).{6,}$/, "Kết hợp chữ hoa, chữ thường, chữ số và ký hiệu đặc biệt"),
  confirmPassword: z.string().min(1, "Vui lòng xác nhận mật khẩu mới"),
})
.refine((data) => data.newPassword !== data.oldPassword, {
  message: "Mật khẩu mới phải khác mật khẩu hiện tại",
  path: ["newPassword"],
})
.refine((data) => data.newPassword === data.confirmPassword, {
  message: "Mật khẩu xác nhận không khớp",
  path: ["confirmPassword"],
});

function ChangePasswordSettings() {
  const navigate = useNavigate();
  const { logoutLocal } = useAuth();

  const [submitError, setSubmitError] = useState("");
  const [submitSuccess, setSubmitSuccess] = useState("");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(passwordSchema),
    defaultValues: {
      oldPassword: "",
      newPassword: "",
      confirmPassword: "",
    },
  });

  const onSubmit = async (data) => {
    setSubmitError("");
    setSubmitSuccess("");

    try {
      await changePassword({
        oldPassword: data.oldPassword,
        newPassword: data.newPassword,
        confirmPassword: data.confirmPassword,
      });

      setSubmitSuccess(
        "Đổi mật khẩu thành công! Bạn sẽ được chuyển đến trang đăng nhập...",
      );

      /**
       * Đổi mật khẩu làm server vô hiệu hoá mọi token cũ,
       * nên đăng xuất cục bộ ngay để người dùng đăng nhập lại.
       */
      setTimeout(() => {
        logoutLocal();
        navigate(ROUTES.LOGIN, { replace: true });
      }, 1500);
    } catch (error) {
      const serverMessage = error.response?.data?.message;
      const errorTranslations = {
        "Invalid password.": "Mật khẩu hiện tại không đúng.",
        "New password must be different from the current password.":
          "Mật khẩu mới phải khác mật khẩu hiện tại.",
      };
      const errorMessage =
        errorTranslations[serverMessage] ||
        serverMessage ||
        "Có lỗi trong quá trình đổi mật khẩu, vui lòng thử lại";
      setSubmitError(errorMessage);
    }
  };

  const handleCancel = () => {
    navigate(ROUTES.PROFILE);
  };

  return (
    <div className="change-password-wrapper">
      <Card className="change-password-card">
        <h2 className="change-password-title">Đổi mật khẩu</h2>

        <div className="change-password-divider"></div>

        <form onSubmit={handleSubmit(onSubmit)} onChange={() => { setSubmitError(""); setSubmitSuccess(""); }} noValidate>
          <div className="form-fields-container">
            <div className="form-group row">
              <label className="field-label">
                Mật khẩu hiện tại <span className="text-danger">*</span>
              </label>
              <div className="field-input">
                <PasswordInput
                  placeholder="Nhập mật khẩu hiện tại"
                  error={errors.oldPassword?.message}
                  {...register("oldPassword")}
                />
                <div className="forgot-password-link-container">
                  <Link to={ROUTES.PROFILE_FORGOT_PASSWORD} className="btn-forgot-password">
                    Quên mật khẩu?
                  </Link>
                </div>
              </div>
            </div>

            <div className="form-group row">
              <label className="field-label">
                Mật khẩu mới <span className="text-danger">*</span>
              </label>
              <div className="field-input">
                <PasswordInput
                  placeholder="Nhập mật khẩu mới"
                  error={errors.newPassword?.message}
                  {...register("newPassword")}
                />
                {!errors.newPassword && (
                  <div className="password-hints">
                    <p>• Mật khẩu nên đặt từ 6 kí tự trở lên</p>
                    <p>• Kết hợp chữ hoa, chữ thường, chữ số và ký hiệu đặc biệt (@, !, _,...)</p>
                  </div>
                )}
              </div>
            </div>

            <div className="form-group row">
              <label className="field-label">
                Xác nhận mật khẩu <span className="text-danger">*</span>
              </label>
              <div className="field-input">
                <PasswordInput
                  placeholder="Nhập lại mật khẩu mới"
                  error={errors.confirmPassword?.message}
                  {...register("confirmPassword")}
                />
              </div>
            </div>
          </div>

          {submitError && <FormMessage variant="error">{submitError}</FormMessage>}
          {submitSuccess && <FormMessage variant="success">{submitSuccess}</FormMessage>}

          <div className="form-actions">
            <Button
              type="submit"
              disabled={isSubmitting || !!submitSuccess}
              aria-busy={isSubmitting}
            >
              {isSubmitting ? "Đang xử lý..." : "Đổi mật khẩu"}
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

export default ChangePasswordSettings;
