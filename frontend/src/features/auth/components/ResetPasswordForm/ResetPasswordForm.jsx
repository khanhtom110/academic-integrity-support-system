import "./ResetPasswordForm.css";

import { Link, useNavigate } from "react-router-dom";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import PasswordInput from "../../../../components/ui/PasswordInput";
import Button from "../../../../components/ui/Button";
import Card from "../../../../components/ui/Card";

import { ROUTES } from "../../../../constants/routes";
import { resetPasswordSchema } from "../../validation/resetPasswordSchema";

function ResetPasswordForm() {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      password: "",
      confirmPassword: "",
    },
    mode: "onSubmit",
    reValidateMode: "onChange",
  });

  const onSubmit = (data) => {
    console.log(data);

    // TODO
    // authService.resetPassword(data);

    navigate(ROUTES.LOGIN);
  };

  return (
    <Card className="reset-password-form">
      <form onSubmit={handleSubmit(onSubmit)}>
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

        <Button type="submit">Khôi phục mật khẩu</Button>

        <div className="login-link body-2">
          <span>Nhớ mật khẩu?</span>

          <Link to={ROUTES.LOGIN}>Đăng nhập</Link>
        </div>
      </form>
    </Card>
  );
}

export default ResetPasswordForm;
