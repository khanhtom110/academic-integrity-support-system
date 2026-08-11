import "./ResetPasswordForm.css";

import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import PasswordInput from "../../../../components/ui/PasswordInput";
import Button from "../../../../components/ui/Button";
import Card from "../../../../components/ui/Card";
import FormMessage from "../../../../components/ui/FormMessage";

import { ROUTES } from "../../../../constants/routes";
import { resetPassword } from "../../services/authService";
import { resetPasswordSchema } from "../../validation/resetPasswordSchema";

function ResetPasswordForm() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const email = state?.email;
  const canResetPassword =
    state?.purpose === "reset-password" && state?.otpVerified === true;
  const [submitError, setSubmitError] = useState("");

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
      navigate(ROUTES.FORGOT_PASSWORD, { replace: true });
    }
  }, [canResetPassword, email, navigate]);

  if (!email || !canResetPassword) {
    return null;
  }

  const onSubmit = async ({ password, confirmPassword }) => {
    setSubmitError("");

    try {
      await resetPassword({
        email,
        newPassword: password,
        confirmPassword,
      });

      navigate(ROUTES.RESET_PASSWORD_SUCCESS, { replace: true });
    } catch (error) {
      setSubmitError(
        error.response?.data?.message ||
          "Không thể đặt lại mật khẩu. Vui lòng thử lại.",
      );
    }
  };

  return (
    <Card className="reset-password-form">
      <form
        onSubmit={handleSubmit(onSubmit)}
        onChange={() => submitError && setSubmitError("")}
        noValidate
      >
        <h2 className="heading-2 reset-title">Đặt lại mật khẩu</h2>

        <PasswordInput
          id="password"
          label="Mật khẩu mới"
          placeholder="Nhập mật khẩu mới"
          error={errors.password?.message}
          {...register("password")}
        />

        <div className="password-note">
          <p>- Mật khẩu phải từ 8 kí tự trở lên.</p>
          <p>
            - Kết hợp chữ hoa, chữ thường, chữ số và ký hiệu đặc biệt (@, !,
            ...)
          </p>
        </div>

        <PasswordInput
          id="confirmPassword"
          label="Xác nhận mật khẩu"
          placeholder="Nhập lại mật khẩu mới"
          error={errors.confirmPassword?.message}
          {...register("confirmPassword")}
        />

        <FormMessage>{submitError}</FormMessage>

        <Button type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
          {isSubmitting ? "Đang cập nhật..." : "Khôi phục mật khẩu"}
        </Button>

        <div className="login-link body-2">
          <span>Nhớ mật khẩu?</span>

          <Link to={ROUTES.LOGIN}>Đăng nhập</Link>
        </div>
      </form>
    </Card>
  );
}

export default ResetPasswordForm;
