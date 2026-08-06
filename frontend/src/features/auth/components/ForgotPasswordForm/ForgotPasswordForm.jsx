import "./ForgotPasswordForm.css";

import { Link } from "react-router-dom";
import ArrowBackRoundedIcon from "@mui/icons-material/ArrowBackRounded";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "../../../../constants/routes";
import { forgotPasswordSchema } from "../../validation/forgotPasswordSchema";
import BackToLogin from "../../../../components/ui/BackToLogin";
import Card from "../../../../components/ui/Card";
import Input from "../../../../components/ui/Input";
import Button from "../../../../components/ui/Button";

function ForgotPasswordForm() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(forgotPasswordSchema),
    mode: "onBlur",
    defaultValues: {
      email: "",
    },
  });
  const navigate = useNavigate();
  const onSubmit = (data) => {
    console.log(data);
    navigate(ROUTES.EMAIL_SENT);
  };

  return (
    <Card className="forgot-password-form">
      <form onSubmit={handleSubmit(onSubmit)}>
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
        <Button type="submit">Gửi liên kết</Button>
        <BackToLogin />
      </form>
    </Card>
  );
}

export default ForgotPasswordForm;
