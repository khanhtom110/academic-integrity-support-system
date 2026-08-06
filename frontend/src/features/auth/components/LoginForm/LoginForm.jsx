import "./LoginForm.css";

import { Link, useNavigate } from "react-router-dom";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import Card from "../../../../components/ui/Card";
import Input from "../../../../components/ui/Input";
import PasswordInput from "../../../../components/ui/PasswordInput";
import Button from "../../../../components/ui/Button";
import Divider from "../../../../components/ui/Divider";
import SocialButton from "../../../../components/ui/SocialButton";

import { loginSchema } from "../../validation/loginSchema";
import { login as loginService } from "../../services/authService";

import { ROUTES } from "../../../../constants/routes";
import { useAuth } from "../../../../hooks/useAuth";

function LoginForm() {
  const navigate = useNavigate();

  const auth = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(loginSchema),
    mode: "onBlur",
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const onSubmit = async (formData) => {
    try {
      const response = await loginService(formData);

      console.log("===== LOGIN RESPONSE =====");
      console.log(response);

      auth.login(response.data);

      console.log("===== AUTH.LOGIN DONE =====");

      navigate(ROUTES.HOME);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Card className="login-form">
      <form onSubmit={handleSubmit(onSubmit)}>
        <h2 className="heading-2 login-title">Đăng nhập</h2>

        <Input
          label="Email"
          placeholder="Nhập địa chỉ email"
          error={errors.email?.message}
          {...register("email")}
        />

        <PasswordInput
          label="Mật khẩu"
          placeholder="Nhập mật khẩu"
          error={errors.password?.message}
          {...register("password")}
        />

        <div className="forgot-password">
          <Link to={ROUTES.FORGOT_PASSWORD}>Quên mật khẩu?</Link>
        </div>

        <Button type="submit">
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

          <SocialButton provider="facebook" />
        </div>
      </form>
    </Card>
  );
}

export default LoginForm;
