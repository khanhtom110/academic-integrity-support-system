import "./EmailSent.css";

import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";

import Card from "../../../../components/ui/Card";
import Button from "../../../../components/ui/Button";
import AuthSteps from "../../../../components/ui/AuthSteps";
import { MailIcon } from "../../../../components/ui/Icons";

import { ROUTES } from "../../../../constants/routes";

function EmailSent() {
  const { state } = useLocation();
  const navigate = useNavigate();

  if (!state?.email || state?.purpose !== "reset-password") {
    return <Navigate to={ROUTES.FORGOT_PASSWORD} replace />;
  }

  const emailDomain = state.email.split("@")[1]?.toLowerCase() || "";
  const mailbox =
    emailDomain === "gmail.com"
      ? {
          label: "Mở Gmail",
          url: "https://mail.google.com/mail/u/0/#inbox",
        }
      : ["outlook.com", "hotmail.com", "live.com"].includes(emailDomain)
        ? { label: "Mở Outlook", url: "https://outlook.live.com/mail/" }
        : null;

  const continueToOtp = () => navigate(ROUTES.OTP, { state });

  return (
    <Card className="email-sent">
      <AuthSteps currentStep={1} />
      <div className="success-icon">
        <MailIcon />
      </div>

      <h2 className="heading-2">Kiểm tra email</h2>

      <p className="body-2 email-description">
        Chúng tôi đã gửi mã OTP đặt lại mật khẩu tới {state.email}.
      </p>

      {mailbox ? (
        <a
          className="primary-btn gmail-link"
          href={mailbox.url}
          target="_blank"
          rel="noopener noreferrer"
          onClick={continueToOtp}
        >
          {mailbox.label}
        </a>
      ) : (
        <Button onClick={continueToOtp}>Tiếp tục nhập OTP</Button>
      )}

      <div className="continue-link">
        <Link to={ROUTES.OTP} state={state}>
          Tiếp tục
        </Link>
      </div>
    </Card>
  );
}

export default EmailSent;
