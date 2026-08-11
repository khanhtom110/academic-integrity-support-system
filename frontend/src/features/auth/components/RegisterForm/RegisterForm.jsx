import "./RegisterForm.css";

import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import Card from "../../../../components/ui/Card";
import Input from "../../../../components/ui/Input";
import PasswordInput from "../../../../components/ui/PasswordInput";
import Button from "../../../../components/ui/Button";
import Divider from "../../../../components/ui/Divider";
import FormMessage from "../../../../components/ui/FormMessage";
import SocialButton from "../../../../components/ui/SocialButton";

import { registerSchema } from "../../validation/registerSchema";
import { register as registerService } from "../../services/authService";
import { getVietnameseAuthError } from "../../utils/authMessages";

import { ROUTES } from "../../../../constants/routes";

function RegisterForm() {
  const navigate = useNavigate();
  const [submitError, setSubmitError] = useState("");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(registerSchema),
    mode: "onSubmit",
    reValidateMode: "onChange",
    defaultValues: {
      fullName: "",
      email: "",
      password: "",
      confirmPassword: "",
    },
  });

  const onSubmit = async (formData) => {
    setSubmitError("");

    try {
      await registerService(formData);
      /**
       * Chuyển sang trang OTP
       * Sau này sẽ truyền email nếu cần.
       */
      navigate(ROUTES.OTP, {
        state: {
          email: formData.email,
        },
      });
    } catch (error) {
      setSubmitError(getVietnameseAuthError(error, "register"));
    }
  };

  return (
    <Card className="register-form">
      <form
        onSubmit={handleSubmit(onSubmit)}
        onChange={() => submitError && setSubmitError("")}
        noValidate
      >
        <h2 className="heading-2 register-title">Đăng ký</h2>

        <Input
          label="Họ và tên"
          autoComplete="name"
          placeholder="Nhập họ và tên"
          error={errors.fullName?.message}
          {...register("fullName")}
        />

        <Input
          label="Email"
          type="email"
          autoComplete="email"
          placeholder="Nhập địa chỉ email"
          error={errors.email?.message}
          {...register("email")}
        />

        <PasswordInput
          label="Mật khẩu"
          placeholder="Nhập mật khẩu"
          error={errors.password?.message}
          autoComplete="new-password"
          {...register("password")}
        />

        <PasswordInput
          label="Xác nhận mật khẩu"
          placeholder="Nhập lại mật khẩu"
          error={errors.confirmPassword?.message}
          autoComplete="new-password"
          {...register("confirmPassword")}
        />

        <div className="password-note">
          <p>Mật khẩu phải có tối thiểu 8 ký tự.</p>
          <p>Nên bao gồm chữ hoa, chữ thường và số.</p>
        </div>

        <FormMessage>{submitError}</FormMessage>

        <Button type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
          {isSubmitting ? "Đang đăng ký..." : "Đăng ký"}
        </Button>

        <div className="register-login body-2">
          <span>Đã có tài khoản?</span>

          <Link to={ROUTES.LOGIN}>Đăng nhập</Link>
        </div>

        <Divider text="Hoặc đăng ký với" />

        <div className="social-login">
          <SocialButton provider="google" />

          <SocialButton provider="outlook" />

          <SocialButton provider="facebook" />
        </div>
      </form>
    </Card>
  );
}

export default RegisterForm;
