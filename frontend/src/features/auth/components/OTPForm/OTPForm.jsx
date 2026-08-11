import "./OTPForm.css";

import { useEffect, useState } from "react";

import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import Card from "../../../../components/ui/Card";
import Button from "../../../../components/ui/Button";
import FormMessage from "../../../../components/ui/FormMessage";
import OTPInput from "../../../../components/ui/OTPInput";
import BackToLogin from "../../../../components/ui/BackToLogin";
import AuthSteps from "../../../../components/ui/AuthSteps";
import { useLocation, useNavigate } from "react-router-dom";
import {
  forgotPassword,
  resendOtp,
  verifyOtp,
  verifyResetOtp,
} from "../../services/authService";
import { otpSchema } from "../../validation/otpSchema";

import { ROUTES } from "../../../../constants/routes";

function OTPForm() {
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
      navigate(
        isPasswordReset ? ROUTES.FORGOT_PASSWORD : ROUTES.REGISTER,
        { replace: true },
      );
    }
  }, [email, isPasswordReset, navigate]);

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
      const verifyService = isPasswordReset ? verifyResetOtp : verifyOtp;

      await verifyService({
        email,
        otp: data.otp,
      });

      if (isPasswordReset) {
        navigate(ROUTES.RESET_PASSWORD, {
          state: {
            email,
            purpose: "reset-password",
            otpVerified: true,
          },
        });
        return;
      }

      navigate(ROUTES.LOGIN);
    } catch (error) {
      setFormMessage({
        type: "error",
        text: error.response?.data?.message || "Xác thực OTP thất bại.",
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
      const resendService = isPasswordReset ? forgotPassword : resendOtp;
      const response = await resendService({ email });

      setFormMessage({
        type: "success",
        text: response.message || "Mã OTP mới đã được gửi.",
      });
      setResendCountdown(60);
    } catch (error) {
      setFormMessage({
        type: "error",
        text: error.response?.data?.message || "Không thể gửi lại OTP.",
      });
    } finally {
      setIsResending(false);
    }
  };

  return (
    <Card className="otp-form">
      <form
        onSubmit={handleSubmit(onSubmit)}
        onChange={() => formMessage && setFormMessage(null)}
        noValidate
      >
        {isPasswordReset && <AuthSteps currentStep={2} />}
        <h2 className="heading-2">Nhập mã OTP</h2>

        <p className="body-2 otp-description">
          Nhập mã gồm 6 chữ số được gửi tới email của bạn.
        </p>

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

        {errors.otp && (
          <p id="otp-error" className="otp-error" role="alert">
            {errors.otp.message}
          </p>
        )}

        <FormMessage variant={formMessage?.type}>
          {formMessage?.text}
        </FormMessage>

        <Button type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
          {isSubmitting ? "Đang xác nhận..." : "Xác nhận"}
        </Button>
        <div className="resend-code">
          <span>Không nhận được mã?</span>

          <button
            type="button"
            className="resend-button"
            onClick={handleResendOtp}
            disabled={isResending || resendCountdown > 0}
            aria-busy={isResending}
          >
            {isResending
              ? "Đang gửi..."
              : resendCountdown > 0
                ? `Gửi lại sau ${resendCountdown}s`
                : "Gửi lại"}
          </button>
        </div>
        <BackToLogin />
      </form>
    </Card>
  );
}

export default OTPForm;
