import "./LoginForm.css";

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

import { loginSchema } from "../../validation/loginSchema";
import { login as loginService } from "../../services/authService";
import { getVietnameseAuthError } from "../../utils/authMessages";

import { ROUTES } from "../../../../constants/routes";
import { useAuth } from "../../../../hooks/useAuth";

function LoginForm() {
  const navigate = useNavigate();

  const auth = useAuth();
  const [submitError, setSubmitError] = useState("");

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(loginSchema),
    mode: "onSubmit",
    reValidateMode: "onChange",
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (formData) => {
    setSubmitError("");

    try {
      const response = await loginService(formData);

      auth.login(response.data);

      navigate(ROUTES.HOME);
    } catch (error) {
      setSubmitError(getVietnameseAuthError(error, "login"));
    }
  };

  return (
    <Card className="login-form">
      <form
        onSubmit={handleSubmit(onSubmit)}
        onChange={() => submitError && setSubmitError("")}
        noValidate
      >
        <h2 className="heading-2 login-title">Đăng nhập</h2>

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
          autoComplete="current-password"
          {...register("password")}
        />

        <div className="forgot-password">
          <Link to={ROUTES.FORGOT_PASSWORD}>Quên mật khẩu?</Link>
        </div>

        <FormMessage>{submitError}</FormMessage>

        <Button type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
          {isSubmitting ? "Đang đăng nhập..." : "Đăng nhập"}
        </Button>

        <div className="register-link body-2">
          <span>Chưa có tài khoản?</span>

          <Link to={ROUTES.REGISTER}>Đăng ký</Link>
        </div>

        <Divider text="Hoặc đăng nhập với" />

        <div className="social-login">
          <SocialButton provider="google" />

          <SocialButton provider="outlook" />
        </div>
      </form>
    </Card>
  );
}

export default LoginForm;
