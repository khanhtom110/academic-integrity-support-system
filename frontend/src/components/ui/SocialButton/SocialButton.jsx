import { useEffect, useState } from "react";

import "./SocialButton.css";

import googleIcon from "../../../assets/icons/google.svg";
import outlookIcon from "../../../assets/icons/Outlook.svg";

import { createOAuthAuthorizationUrl } from "../../../constants/oauthConfig";

const icons = {
  google: googleIcon,
  outlook: outlookIcon,
};

const labels = {
  google: "Google",
  outlook: "Outlook",
};

function SocialButton({ provider }) {
  const [isRedirecting, setIsRedirecting] = useState(false);
  const [loginError, setLoginError] = useState("");

  useEffect(() => {
    const resetRedirecting = () => setIsRedirecting(false);
    const resetWhenVisible = () => {
      if (document.visibilityState === "visible") {
        resetRedirecting();
      }
    };

    window.addEventListener("pageshow", resetRedirecting);
    window.addEventListener("focus", resetRedirecting);
    document.addEventListener("visibilitychange", resetWhenVisible);

    return () => {
      window.removeEventListener("pageshow", resetRedirecting);
      window.removeEventListener("focus", resetRedirecting);
      document.removeEventListener("visibilitychange", resetWhenVisible);
    };
  }, []);

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
