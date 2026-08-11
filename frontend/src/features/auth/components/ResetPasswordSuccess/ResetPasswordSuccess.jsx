import "./ResetPasswordSuccess.css";

import { Link, Navigate, useLocation } from "react-router-dom";

import { CheckIcon } from "../../../../components/ui/Icons";

import Card from "../../../../components/ui/Card";

import { ROUTES } from "../../../../constants/routes";

function ResetPasswordSuccess() {
  const { state } = useLocation();

  if (state?.resetCompleted !== true) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  return (
    <Card className="reset-success">
      <h2 className="heading-2 reset-success-title">Mật khẩu đã thay đổi</h2>

      <div className="success-icon">
        <CheckIcon />
      </div>

      <p className="body-2 success-message">
        Mật khẩu của bạn đã được thay đổi thành công.
      </p>

      <Link to={ROUTES.LOGIN} className="primary-btn login-button-link">
        Đăng nhập
      </Link>
    </Card>
  );
}

export default ResetPasswordSuccess;
