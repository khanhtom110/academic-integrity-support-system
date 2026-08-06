import "./RegisterForm.css";

import { Link } from "react-router-dom";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { registerSchema } from "../../validation/registerSchema";
import { ROUTES } from "../../../../constants/routes";

import Card from "../../../../components/ui/Card";
import Input from "../../../../components/ui/Input";
import PasswordInput from "../../../../components/ui/PasswordInput";
import Button from "../../../../components/ui/Button";
import Divider from "../../../../components/ui/Divider";
import SocialButton from "../../../../components/ui/SocialButton";

function RegisterForm() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(registerSchema),

    mode: "onSubmit",

    reValidateMode: "onChange",

    defaultValues: {
      fullName: "",
      username: "",
      email: "",
      password: "",
      confirmPassword: "",
    },
  });

  const onSubmit = (data) => {
    console.log(data);
  };

  return (
    <Card className="register-form">
      <form onSubmit={handleSubmit(onSubmit)}>
        <h2 className="heading-2 register-title">Đăng ký</h2>

        <Input
          id="fullName"
          label="Họ và tên"
          placeholder="Nhập họ và tên"
          error={errors.fullName?.message}
          {...register("fullName")}
        />

        <Input
          id="username"
          label="Tên đăng nhập"
          placeholder="Nhập tên đăng nhập"
          error={errors.username?.message}
          {...register("username")}
        />

        <Input
          id="email"
          label="Địa chỉ email"
          placeholder="Nhập địa chỉ email"
          error={errors.email?.message}
          {...register("email")}
        />

        <PasswordInput
          id="password"
          label="Mật khẩu"
          placeholder="Nhập mật khẩu"
          error={errors.password?.message}
          {...register("password")}
        />

        <div className="password-note">
          <p>- Mật khẩu phải từ 8 ký tự trở lên.</p>
          <p>
            - Kết hợp chữ hoa, chữ thường, chữ số và ký hiệu đặc biệt (@, !,
            ...)
          </p>
        </div>

        <PasswordInput
          id="confirmPassword"
          label="Xác nhận mật khẩu"
          placeholder="Nhập lại mật khẩu"
          error={errors.confirmPassword?.message}
          {...register("confirmPassword")}
        />

        <Button type="submit">Đăng ký</Button>

        <Divider text="Hoặc đăng nhập với" />

        <div className="social-login">
          <SocialButton provider="google" />
          <SocialButton provider="outlook" />
          <SocialButton provider="facebook" />
        </div>

        <div className="register-login">
          <span>Đã có tài khoản?</span>

          <Link to={ROUTES.LOGIN}>Đăng nhập</Link>
        </div>
      </form>
    </Card>
  );
}

export default RegisterForm;
