import { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useLocation, useNavigate } from "react-router-dom";

import Card from "../../../../components/ui/Card";
import Button from "../../../../components/ui/Button";
import FormMessage from "../../../../components/ui/FormMessage";
import OTPInput from "../../../../components/ui/OTPInput";

import { forgotPassword, verifyResetOtp } from "../../../auth/services/authService";
import { otpSchema } from "../../../auth/validation/otpSchema";
import { getVietnameseAuthError, getVietnameseSuccessMessage } from "../../../auth/utils/authMessages";
import { ROUTES } from "../../../../constants/routes";
import "./ProfileOTPSettings.css";

function ProfileOTPSettings() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const email = state?.email;
  const isPasswordReset = state?.purpose === "reset-password";
  
  const [formMessage, setFormMessage] = useState(null);
  const [isResending, setIsResending] = useState(false);
  const [resendCountdown, setResendCountdown] = useState(60);

  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(otpSchema),
    mode: "onSubmit",
    reValidateMode: "onChange",
    defaultValues: {
      otp: "",
    },
  });

  useEffect(() => {
    if (!email) {
      navigate(ROUTES.PROFILE_FORGOT_PASSWORD, { replace: true });
    }
  }, [email, navigate]);

  useEffect(() => {
    if (resendCountdown <= 0) {
      return undefined;
    }

    const timer = window.setInterval(() => {
      setResendCountdown((current) => Math.max(current - 1, 0));
    }, 1000);

    return () => window.clearInterval(timer);
  }, [resendCountdown]);

  if (!email) {
    return null;
  }

  const onSubmit = async (data) => {
    setFormMessage(null);

    try {
      await verifyResetOtp({
        email,
        otp: data.otp,
      });

      navigate(ROUTES.PROFILE_RESET_PASSWORD, {
        state: {
          email,
          purpose: "reset-password",
          otpVerified: true,
        },
      });
    } catch (error) {
      setFormMessage({
        type: "error",
        text: getVietnameseAuthError(error, "verifyOtp"),
      });
    }
  };

  const handleResendOtp = async () => {
    if (isResending || resendCountdown > 0) {
      return;
    }

    setFormMessage(null);
    setIsResending(true);

    try {
      const response = await forgotPassword({ email });

      setFormMessage({
        type: "success",
        text: getVietnameseSuccessMessage(
          response.message,
          "Mã OTP mới đã được gửi.",
        ),
      });
      setResendCountdown(60);
    } catch (error) {
      setFormMessage({
        type: "error",
        text: getVietnameseAuthError(error, "resendOtp"),
      });
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="profile-otp-wrapper">
      <Card className="profile-otp-card">
        <h2 className="profile-otp-title">Quên mật khẩu</h2>
        
        <div className="profile-otp-divider"></div>

        <div className="profile-otp-content">
          <p className="otp-description">
            Chúng tôi đã gửi 1 mã xác minh đến {email}.
            <br />
            Mã xác minh có giá trị trong 01:59
          </p>

          <form onSubmit={handleSubmit(onSubmit)} onChange={() => formMessage && setFormMessage(null)} noValidate>
            <div className="otp-input-container">
              <Controller
                name="otp"
                control={control}
                render={({ field }) => (
                  <OTPInput
                    value={field.value}
                    onChange={field.onChange}
                    hasError={!!errors.otp}
                    errorId="otp-error"
                  />
                )}
              />
            </div>

            {errors.otp && (
              <p id="otp-error" className="otp-error-text" role="alert">
                {errors.otp.message}
              </p>
            )}

            <div className="form-message-container">
              <FormMessage variant={formMessage?.type}>
                {formMessage?.text}
              </FormMessage>
            </div>

            <div className="form-actions">
              <Button type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
                {isSubmitting ? "Đang xác nhận..." : "Tiếp tục"}
              </Button>
            </div>

            <div className="resend-code">
              <span>Chưa nhận được mã?</span>
              <button
                type="button"
                className="resend-button"
                onClick={handleResendOtp}
                disabled={isResending || resendCountdown > 0}
                aria-busy={isResending}
              >
                {isResending
                  ? " Đang gửi..."
                  : resendCountdown > 0
                    ? ` Gửi lại sau ${resendCountdown}s`
                    : " Gửi lại"}
              </button>
            </div>
          </form>
        </div>
      </Card>
    </div>
  );
}

export default ProfileOTPSettings;
