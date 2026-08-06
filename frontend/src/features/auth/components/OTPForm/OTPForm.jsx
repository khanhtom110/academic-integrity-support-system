import "./OTPForm.css";

import { Link } from "react-router-dom";

import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import ArrowBackRoundedIcon from "@mui/icons-material/ArrowBackRounded";
import Card from "../../../../components/ui/Card";
import Button from "../../../../components/ui/Button";
import OTPInput from "../../../../components/ui/OTPInput";
import BackToLogin from "../../../../components/ui/BackToLogin";
import { otpSchema } from "../../validation/otpSchema";

import { ROUTES } from "../../../../constants/routes";

function OTPForm() {
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(otpSchema),
    mode: "onSubmit",
    reValidateMode: "onChange",
    defaultValues: {
      otp: "",
    },
  });

  const onSubmit = (data) => {
    console.log("OTP:", data.otp);

    // TODO:
    // authService.verifyOtp(data);
  };

  return (
    <Card className="otp-form">
      <form onSubmit={handleSubmit(onSubmit)}>
        <h2 className="heading-2">Nhập mã OTP</h2>

        <p className="body-2 otp-description">
          Nhập mã gồm 6 chữ số được gửi tới email của bạn.
        </p>

        <Controller
          name="otp"
          control={control}
          render={({ field }) => (
            <OTPInput
              value={field.value}
              onChange={field.onChange}
              hasError={!!errors.otp}
            />
          )}
        />

        {errors.otp && <p className="otp-error">{errors.otp.message}</p>}
        <Button type="submit">Xác nhận</Button>
        <div className="resend-code">
          <span>Không nhận được mã?</span>

          <Link to="#">Gửi lại</Link>
        </div>
        <BackToLogin />
      </form>
    </Card>
  );
}

export default OTPForm;
