import { useState } from "react";
import { Link, useNavigate, useOutletContext } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import Button from "../../../../components/ui/Button";
import FormMessage from "../../../../components/ui/FormMessage";
import PasswordInput from "../../../../components/ui/PasswordInput";
import { ROUTES } from "../../../../constants/routes";
import { changePassword } from "../../services/userService";
import { getChangePasswordError } from "../../utils/changePasswordMessages";
import { changePasswordSchema } from "../../validation/changePasswordSchema";

import "./ChangePasswordForm.css";

function ChangePasswordForm() {
  const navigate = useNavigate();
  const { profile } = useOutletContext();
  const [feedback, setFeedback] = useState(null);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(changePasswordSchema),
    mode: "onSubmit",
    reValidateMode: "onChange",
    defaultValues: {
      oldPassword: "",
      newPassword: "",
      confirmPassword: "",
    },
  });

  const onSubmit = async (data) => {
    setFeedback(null);

    try {
      await changePassword(data);
      reset();
      setFeedback({ type: "success", message: "Mật khẩu của bạn đã được thay đổi." });
    } catch (error) {
      setFeedback({ type: "error", message: getChangePasswordError(error) });
    }
  };

  return (
    <>
      {feedback && (
        <div className="profile-toast">
          <FormMessage variant={feedback.type}>{feedback.message}</FormMessage>
        </div>
      )}

      <section className="change-password-card" aria-labelledby="change-password-title">
        <h1 id="change-password-title">Đổi mật khẩu</h1>

        <form
          onSubmit={handleSubmit(onSubmit)}
          onChange={() => feedback && setFeedback(null)}
          noValidate
        >
          <div className="change-password-fields">
            <PasswordInput
              label="Mật khẩu hiện tại"
              autoComplete="current-password"
              placeholder="Nhập mật khẩu hiện tại"
              error={errors.oldPassword?.message}
              {...register("oldPassword")}
            />

            <div className="change-password-forgot">
              <Link to={ROUTES.FORGOT_PASSWORD} state={{ email: profile.email || "" }}>
                Quên mật khẩu?
              </Link>
            </div>

            <PasswordInput
              label="Mật khẩu mới"
              autoComplete="new-password"
              placeholder="Nhập mật khẩu mới"
              error={errors.newPassword?.message}
              {...register("newPassword")}
            />
            <div className="change-password-hint" aria-hidden="true">
              <p>• Mật khẩu từ 8–120 ký tự</p>
              <p>• Gồm chữ hoa, chữ thường, số và ký tự đặc biệt</p>
            </div>

            <PasswordInput
              label="Xác nhận mật khẩu"
              autoComplete="new-password"
              placeholder="Nhập lại mật khẩu mới"
              error={errors.confirmPassword?.message}
              {...register("confirmPassword")}
            />
          </div>

          <div className="change-password-actions">
            <Button type="submit" disabled={isSubmitting} aria-busy={isSubmitting}>
              {isSubmitting ? "Đang đổi..." : "Đổi mật khẩu"}
            </Button>
            <button
              type="button"
              className="change-password-cancel"
              onClick={() => navigate(ROUTES.PROFILE)}
              disabled={isSubmitting}
            >
              Hủy
            </button>
          </div>
        </form>
      </section>
    </>
  );
}

export default ChangePasswordForm;
