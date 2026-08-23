import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import Card from "../../../../components/ui/Card";
import PasswordInput from "../../../../components/ui/PasswordInput";
import Button from "../../../../components/ui/Button";
import FormMessage from "../../../../components/ui/FormMessage";

import { resetPassword } from "../../../auth/services/authService";
import { resetPasswordSchema } from "../../../auth/validation/resetPasswordSchema";
import { getVietnameseAuthError } from "../../../auth/utils/authMessages";
import { ROUTES } from "../../../../constants/routes";
import "./ProfileResetPasswordSettings.css";

function ProfileResetPasswordSettings() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const email = state?.email;
  const canResetPassword =
    state?.purpose === "reset-password" && state?.otpVerified === true;
    
  const [submitError, setSubmitError] = useState("");
  const [submitSuccess, setSubmitSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      password: "",
      confirmPassword: "",
    },
    mode: "onSubmit",
    reValidateMode: "onChange",
  });

  useEffect(() => {
    if (!email || !canResetPassword) {
      navigate(ROUTES.PROFILE_FORGOT_PASSWORD, { replace: true });
    }
  }, [canResetPassword, email, navigate]);

  if (!email || !canResetPassword) {
    return null;
  }

  const onSubmit = async ({ password, confirmPassword }) => {
    setSubmitError("");
    setSubmitSuccess(false);

    try {
      await resetPassword({
        email,
        newPassword: password,
        confirmPassword,
      });

      setSubmitSuccess(true);

      // Redirect after 3 seconds so user can see the success message
      setTimeout(() => {
        navigate(ROUTES.PROFILE, {
          replace: true,
        });
      }, 3000);
    } catch (error) {
      setSubmitError(getVietnameseAuthError(error, "resetPassword"));
    }
  };

  const handleCancel = () => {
    navigate(ROUTES.PROFILE);
  };

  return (
    <div className="profile-reset-password-wrapper">
      <Card className="profile-reset-password-card">
        <h2 className="profile-reset-password-title">Đặt lại mật khẩu</h2>
        
        <div className="profile-reset-password-divider"></div>

        <form onSubmit={handleSubmit(onSubmit)} onChange={() => submitError && setSubmitError("")} noValidate>
          <div className="form-fields-container">
            <div className="form-group row">
              <label className="field-label">
                Mật khẩu mới <span className="text-danger">*</span>
              </label>
              <div className="field-input">
                <PasswordInput
                  id="password"
                  placeholder="Nhập mật khẩu mới"
                  error={errors.password?.message}
                  autoComplete="new-password"
                  {...register("password")}
                />
                {!errors.password && (
                  <div className="password-hints">
                    <p>• Mật khẩu nên đặt từ 8 kí tự trở lên</p>
                    <p>• Kết hợp chữ hoa, chữ thường, chữ số và ký hiệu đặc biệt (@, !, _...)</p>
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
                  id="confirmPassword"
                  placeholder="Nhập lại mật khẩu mới"
                  error={errors.confirmPassword?.message}
                  autoComplete="new-password"
                  {...register("confirmPassword")}
                />
              </div>
            </div>
          </div>

          <div className="form-message-container">
            {submitError && <FormMessage variant="error">{submitError}</FormMessage>}
            {submitSuccess && <FormMessage variant="success">Mật khẩu của bạn đã được thay đổi thành công</FormMessage>}
          </div>

          <div className="form-actions">
            <Button type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
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

export default ProfileResetPasswordSettings;
