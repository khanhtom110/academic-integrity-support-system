import "./ResetPasswordSuccess.css";

import { Link } from "react-router-dom";

import CheckRoundedIcon from "@mui/icons-material/CheckRounded";

import Card from "../../../../components/ui/Card";
import Button from "../../../../components/ui/Button";

import { ROUTES } from "../../../../constants/routes";

function ResetPasswordSuccess() {
  return (
    <Card className="reset-success">
      <h2 className="heading-2 reset-success-title">Mật khẩu đã thay đổi</h2>

      <div className="success-icon">
        <CheckRoundedIcon sx={{ fontSize: 42, color: "#ffffff" }} />
      </div>

      <p className="body-2 success-message">
        Mật khẩu của bạn đã được thay đổi thành công.
      </p>

      <Link to={ROUTES.LOGIN} className="login-button-link">
        <Button>Đăng nhập</Button>
      </Link>
    </Card>
  );
}

export default ResetPasswordSuccess;
