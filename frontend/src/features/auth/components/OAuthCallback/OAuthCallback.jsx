import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

import "./OAuthCallback.css";

import Button from "../../../../components/ui/Button";
import { ROUTES } from "../../../../constants/routes";
import {
  consumeOAuthState,
  getAppOrigin,
} from "../../../../constants/oauthConfig";
import { useAuth } from "../../../../hooks/useAuth";
import {
  loginWithFacebook,
  loginWithGoogle,
  loginWithOutlook,
} from "../../services/authService";

const loginByProvider = {
  google: loginWithGoogle,
  facebook: loginWithFacebook,
  outlook: loginWithOutlook,
};

const tokenExchangeErrors = [
  "Cannot convert Authorization Code to Token",
  "Cannot convert Oath code",
];

function getOAuthErrorMessage(error) {
  const backendMessage = error.response?.data?.message;
  const appOrigin = getAppOrigin();

  if (tokenExchangeErrors.includes(backendMessage)) {
    return (
      "Mã đăng nhập không thể đổi thành token. Vui lòng bắt đầu lại từ " +
      `${appOrigin} và không sử dụng lại URL callback cũ.`
    );
  }

  return (
    backendMessage ||
    error.message ||
    "Đăng nhập bằng tài khoản bên thứ ba thất bại."
  );
}

function OAuthCallback({ provider }) {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [searchParams] = useSearchParams();
  const hasProcessed = useRef(false);
  const [errorMessage, setErrorMessage] = useState("");

  const code = searchParams.get("code");
  const returnedState = searchParams.get("state");
  const providerError = searchParams.get("error");
  const providerErrorDescription = searchParams.get("error_description");
  const appOrigin = getAppOrigin();

  useEffect(() => {
    if (hasProcessed.current) {
      return;
    }

    hasProcessed.current = true;

    // Không giữ authorization code/state trong history hoặc referrer.
    window.history.replaceState(null, document.title, window.location.pathname);

    const handleOAuthLogin = async () => {
      if (!consumeOAuthState(provider, returnedState)) {
        setErrorMessage(
          "Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng bắt đầu " +
            `lại trực tiếp từ ${appOrigin}.`,
        );
        return;
      }

      if (providerError) {
        setErrorMessage(
          providerError === "access_denied"
            ? "Bạn đã hủy hoặc từ chối cấp quyền đăng nhập."
            : providerErrorDescription || "Nhà cung cấp OAuth đã từ chối đăng nhập.",
        );
        return;
      }

      if (!code) {
        setErrorMessage("Không nhận được mã xác thực từ nhà cung cấp OAuth.");
        return;
      }

      const loginService = loginByProvider[provider];

      if (!loginService) {
        setErrorMessage("Nhà cung cấp đăng nhập không được hỗ trợ.");
        return;
      }

      try {
        const response = await loginService(code);

        if (!response?.data?.accessToken || !response?.data?.refreshToken) {
          throw new Error("Phản hồi đăng nhập từ máy chủ không hợp lệ.");
        }

        login(response.data);
        navigate(ROUTES.HOME, { replace: true });
      } catch (error) {
        setErrorMessage(getOAuthErrorMessage(error));
      }
    };

    handleOAuthLogin();
  }, [
    appOrigin,
    code,
    login,
    navigate,
    provider,
    providerError,
    providerErrorDescription,
    returnedState,
  ]);

  if (errorMessage) {
    return (
      <section className="oauth-callback" role="alert">
        <h2 className="heading-2">Đăng nhập thất bại</h2>
        <p>{errorMessage}</p>
        <Button onClick={() => navigate(ROUTES.LOGIN, { replace: true })}>
          Quay lại đăng nhập
        </Button>
      </section>
    );
  }

  return (
    <section className="oauth-callback" aria-live="polite">
      <span className="oauth-spinner" aria-hidden="true" />
      <h2 className="heading-2">Đang đăng nhập...</h2>
      <p>Vui lòng chờ trong giây lát.</p>
    </section>
  );
}

export default OAuthCallback;
