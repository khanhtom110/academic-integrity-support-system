import "./EmailSent.css";

import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";

import MarkEmailReadRoundedIcon from "@mui/icons-material/MarkEmailReadRounded";

import Card from "../../../../components/ui/Card";

import { ROUTES } from "../../../../constants/routes";

function EmailSent() {
  const { state } = useLocation();
  const navigate = useNavigate();

  if (!state?.email || state?.purpose !== "reset-password") {
    return <Navigate to={ROUTES.FORGOT_PASSWORD} replace />;
  }

  return (
    <Card className="email-sent">
      <div className="success-icon">
        <MarkEmailReadRoundedIcon
          sx={{
            fontSize: 42,
            color: "var(--success-500)",
          }}
        />
      </div>

      <h2 className="heading-2">Kiểm tra email</h2>

      <p className="body-2 email-description">
        Chúng tôi đã gửi mã OTP đặt lại mật khẩu tới {state.email}.
      </p>

      <a
        className="primary-btn gmail-link"
        href="https://mail.google.com/mail/u/0/#inbox"
        target="_blank"
        rel="noopener noreferrer"
        onClick={() => navigate(ROUTES.OTP, { state })}
      >
        Mở Gmail
      </a>

      <div className="continue-link">
        <Link to={ROUTES.OTP} state={state}>
          Tiếp tục
        </Link>
      </div>
    </Card>
  );
}

export default EmailSent;
