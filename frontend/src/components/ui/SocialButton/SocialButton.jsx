import { useState } from "react";

import "./SocialButton.css";

import googleIcon from "../../../assets/icons/google.svg";
import outlookIcon from "../../../assets/icons/Outlook.svg";
import facebookIcon from "../../../assets/icons/Facebook.svg";

import { createOAuthAuthorizationUrl } from "../../../constants/oauthConfig";

const icons = {
  google: googleIcon,
  outlook: outlookIcon,
  facebook: facebookIcon,
};

const labels = {
  google: "Google",
  outlook: "Outlook",
  facebook: "Facebook",
};

function SocialButton({ provider }) {
  const [isRedirecting, setIsRedirecting] = useState(false);
  const [loginError, setLoginError] = useState("");

  const handleLogin = () => {
    try {
      setLoginError("");
      setIsRedirecting(true);
      window.location.assign(createOAuthAuthorizationUrl(provider));
    } catch {
      setIsRedirecting(false);
      setLoginError(
        `Không thể bắt đầu đăng nhập bằng ${labels[provider]}. Vui lòng kiểm tra cấu hình OAuth.`,
      );
    }
  };

  return (
    <div className="social-btn-wrapper">
      <button
        type="button"
        className="social-btn"
        onClick={handleLogin}
        disabled={isRedirecting}
        aria-label={`Đăng nhập bằng ${labels[provider]}`}
        aria-describedby={loginError ? `oauth-error-${provider}` : undefined}
      >
        <img src={icons[provider]} alt="" aria-hidden="true" />

        <span>{isRedirecting ? "Đang chuyển..." : labels[provider]}</span>
      </button>

      {loginError && (
        <span
          id={`oauth-error-${provider}`}
          className="social-btn-error"
          role="alert"
        >
          {loginError}
        </span>
      )}
    </div>
  );
}

export default SocialButton;
