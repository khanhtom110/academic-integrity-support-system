import "./BackToLogin.css";

import { Link } from "react-router-dom";
import ArrowBackRoundedIcon from "@mui/icons-material/ArrowBackRounded";

import { ROUTES } from "../../../constants/routes";

function BackToLogin() {
  return (
    <div className="back-login">
      <Link to={ROUTES.LOGIN}>
        <ArrowBackRoundedIcon fontSize="small" />
        <span>Quay lại đăng nhập</span>
      </Link>
    </div>
  );
}

export default BackToLogin;
