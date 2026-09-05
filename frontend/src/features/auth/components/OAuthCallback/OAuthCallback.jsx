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
  loginWithGoogle,
  loginWithOutlook,
} from "../../services/authService";
import { getVietnameseAuthError } from "../../utils/authMessages";

const loginByProvider = {
  google: loginWithGoogle,
  outlook: loginWithOutlook,
};

function OAuthCallback({ provider }) {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [searchParams] = useSearchParams();
  const hasProcessed = useRef(false);
  const [errorMessage, setErrorMessage] = useState("");

  const code = searchParams.get("code");
  const returnedState = searchParams.get("state");
  const providerError = searchParams.get("error");
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
            : "Nhà cung cấp OAuth đã từ chối đăng nhập. Vui lòng thử lại.",
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
        setErrorMessage(
          getVietnameseAuthError(error, "oauth", { appOrigin }),
        );
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
