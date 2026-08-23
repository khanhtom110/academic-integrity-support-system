import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import Card from "../../../../components/ui/Card";
import Input from "../../../../components/ui/Input";
import Button from "../../../../components/ui/Button";
import FormMessage from "../../../../components/ui/FormMessage";

import { forgotPasswordSchema } from "../../../auth/validation/forgotPasswordSchema";
import { forgotPassword } from "../../../auth/services/authService";
import { getVietnameseAuthError } from "../../../auth/utils/authMessages";
import { ROUTES } from "../../../../constants/routes";
import "./ProfileForgotPasswordSettings.css";

function ProfileForgotPasswordSettings() {
  const navigate = useNavigate();
  const [submitError, setSubmitError] = useState("");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(forgotPasswordSchema),
    mode: "onSubmit",
    reValidateMode: "onChange",
    defaultValues: {
      email: "",
    },
  });

  const onSubmit = async ({ email }) => {
    setSubmitError("");

    try {
      await forgotPassword({ email });
      navigate(ROUTES.PROFILE_OTP, {
        state: { email, purpose: "reset-password" },
      });
    } catch (error) {
      setSubmitError(getVietnameseAuthError(error, "forgotPassword"));
    }
  };

  const handleCancel = () => {
    navigate(ROUTES.CHANGE_PASSWORD);
  };

  return (
    <div className="profile-forgot-password-wrapper">
      <Card className="profile-forgot-password-card">
        <h2 className="profile-forgot-password-title">Quên mật khẩu</h2>
        
        <div className="profile-forgot-password-divider"></div>

        <div className="profile-forgot-password-content">
          <p className="forgot-description">
            Vui lòng nhập email đã đăng ký tài khoản này để lấy mã xác minh khôi phục mật khẩu
          </p>

          <form onSubmit={handleSubmit(onSubmit)} onChange={() => submitError && setSubmitError("")} noValidate>
            <div className="form-group">
              <label className="field-label">
                Địa chỉ email <span className="text-danger">*</span>
              </label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                placeholder="Nhập email"
                error={errors.email?.message}
                {...register("email")}
              />
            </div>

            {submitError && <FormMessage variant="error">{submitError}</FormMessage>}

            <div className="form-actions">
              <Button type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
                {isSubmitting ? "Đang xử lý..." : "Lấy mã xác minh"}
              </Button>
              <button type="button" className="btn-cancel" onClick={handleCancel}>
                Huỷ
              </button>
            </div>
          </form>
        </div>
      </Card>
    </div>
  );
}

export default ProfileForgotPasswordSettings;
