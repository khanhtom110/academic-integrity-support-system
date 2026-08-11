import "./ForgotPasswordForm.css";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "../../../../constants/routes";
import { forgotPasswordSchema } from "../../validation/forgotPasswordSchema";
import BackToLogin from "../../../../components/ui/BackToLogin";
import Card from "../../../../components/ui/Card";
import Input from "../../../../components/ui/Input";
import Button from "../../../../components/ui/Button";
import FormMessage from "../../../../components/ui/FormMessage";
import { forgotPassword } from "../../services/authService";

function ForgotPasswordForm() {
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
      navigate(ROUTES.EMAIL_SENT, {
        state: { email, purpose: "reset-password" },
      });
    } catch (error) {
      setSubmitError(
        error.response?.data?.message ||
          "Không thể gửi mã OTP. Vui lòng thử lại.",
      );
    }
  };

  return (
    <Card className="forgot-password-form">
      <form
        onSubmit={handleSubmit(onSubmit)}
        onChange={() => submitError && setSubmitError("")}
        noValidate
      >
        <h2 className="heading-2 forgot-title">Quên mật khẩu</h2>
        <p className="body-2 forgot-description">
          Vui lòng nhập email đã đăng ký tài khoản này để lấy mã xác minh khôi
          phục mật khẩu
        </p>
        <Input
          id="email"
          label="Email"
          placeholder="Nhập địa chỉ email"
          error={errors.email?.message}
          {...register("email")}
        />

        <FormMessage>{submitError}</FormMessage>

        <Button type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
          {isSubmitting ? "Đang gửi..." : "Gửi mã OTP"}
        </Button>
        <BackToLogin />
      </form>
    </Card>
  );
}

export default ForgotPasswordForm;
