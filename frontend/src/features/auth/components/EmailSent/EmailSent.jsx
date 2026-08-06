import "./EmailSent.css";

import { Link } from "react-router-dom";

import MarkEmailReadRoundedIcon from "@mui/icons-material/MarkEmailReadRounded";

import Card from "../../../../components/ui/Card";
import Button from "../../../../components/ui/Button";

import { ROUTES } from "../../../../constants/routes";

function EmailSent() {
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
        Chúng tôi đã gửi liên kết đặt lại mật khẩu tới địa chỉ email của bạn.
      </p>

      <Button>Mở Email</Button>

      <div className="continue-link">
        <Link to={ROUTES.OTP}>Tiếp tục</Link>
      </div>
    </Card>
  );
}

export default EmailSent;
