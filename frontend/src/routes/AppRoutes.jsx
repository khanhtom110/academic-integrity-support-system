import { Route, Routes } from "react-router-dom";

import { ROUTES } from "../constants/routes";

import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import ForgotPasswordPage from "../pages/ForgotPasswordPage";
import EmailSentPage from "../pages/EmailSentPage";
import OTPPage from "../pages/OTPPage";
import ResetPasswordPage from "../pages/ResetPasswordPage";
import ResetPasswordSuccessPage from "../pages/ResetPasswordSuccessPage";
import OAuthCallbackPage from "../pages/OAuthCallbackPage";
import HomePage from "../pages/HomePage";
import ProtectedRoute from "./ProtectedRoute";

function AppRoutes() {
  return (
    <Routes>
      {/* Trang chủ */}
      <Route
        path={ROUTES.HOME}
        element={
          <ProtectedRoute>
            <HomePage />
          </ProtectedRoute>
        }
      />

      {/* ================= Authentication ================= */}

      <Route path={ROUTES.LOGIN} element={<LoginPage />} />

      <Route path={ROUTES.REGISTER} element={<RegisterPage />} />

      {/* ================= OAuth Callback ================= */}

      <Route
        path={ROUTES.OAUTH_GOOGLE_CALLBACK}
        element={<OAuthCallbackPage provider="google" />}
      />

      <Route
        path={ROUTES.OAUTH_FACEBOOK_CALLBACK}
        element={<OAuthCallbackPage provider="facebook" />}
      />

      <Route
        path={ROUTES.OAUTH_OUTLOOK_CALLBACK}
        element={<OAuthCallbackPage provider="outlook" />}
      />

      {/* ================= Forgot Password ================= */}

      <Route path={ROUTES.FORGOT_PASSWORD} element={<ForgotPasswordPage />} />

      <Route path={ROUTES.EMAIL_SENT} element={<EmailSentPage />} />

      <Route path={ROUTES.OTP} element={<OTPPage />} />

      <Route path={ROUTES.RESET_PASSWORD} element={<ResetPasswordPage />} />

      <Route
        path={ROUTES.RESET_PASSWORD_SUCCESS}
        element={<ResetPasswordSuccessPage />}
      />

      {/* ================= 404 ================= */}

      <Route path="*" element={<h2>404 - Không tìm thấy trang</h2>} />
    </Routes>
  );
}

export default AppRoutes;
