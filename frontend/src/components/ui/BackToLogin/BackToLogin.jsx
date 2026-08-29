import "./BackToLogin.css";

import { Link } from "react-router-dom";
import { ArrowLeftIcon } from "../Icons";

import { ROUTES } from "../../../constants/routes";

function BackToLogin() {
  return (
    <div className="back-login">
      <Link to={ROUTES.LOGIN}>
        <ArrowLeftIcon />
        <span>Quay lại đăng nhập</span>
      </Link>
    </div>
  );
}

export default BackToLogin;
