import "./LoginForm.css";

import { Link } from "react-router-dom";

import Card from "../../../../components/ui/Card";
import Input from "../../../../components/ui/Input";
import PasswordInput from "../../../../components/ui/PasswordInput";
import Button from "../../../../components/ui/Button";
import Divider from "../../../../components/ui/Divider";
import SocialButton from "../../../../components/ui/SocialButton";

function LoginForm() {
  return (
    <Card className="login-form">
      <h2 className="heading-2 login-title">Đăng nhập</h2>

      <Input
        label="Tên đăng nhập hoặc Email"
        placeholder="Nhập tên đăng nhập hoặc email"
      />

      <PasswordInput label="Mật khẩu" placeholder="Nhập mật khẩu" />

      <div className="forgot-password">
        <Link to="/forgot-password">Quên mật khẩu?</Link>
      </div>

      <Button>Đăng nhập</Button>

      <div className="register-link body-2">
        <span>Chưa có tài khoản?</span>

        <Link to="/register">Đăng ký</Link>
      </div>

      <Divider text="Hoặc đăng nhập với" />

      <div className="social-login">
        <SocialButton provider="google" />
        <SocialButton provider="outlook" />
        <SocialButton provider="facebook" />
      </div>
    </Card>
  );
}

export default LoginForm;
