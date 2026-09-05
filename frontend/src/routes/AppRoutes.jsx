import { lazy, Suspense } from "react";
import { Route, Routes } from "react-router-dom";

import { ROUTES } from "../constants/routes";

import ProtectedRoute from "./ProtectedRoute";
import AppLayout from "../components/layout/AppLayout/AppLayout";

const LoginPage = lazy(() => import("../pages/LoginPage"));
const RegisterPage = lazy(() => import("../pages/RegisterPage"));
const ForgotPasswordPage = lazy(() => import("../pages/ForgotPasswordPage"));
const EmailSentPage = lazy(() => import("../pages/EmailSentPage"));
const OTPPage = lazy(() => import("../pages/OTPPage"));
const ResetPasswordPage = lazy(() => import("../pages/ResetPasswordPage"));
const ResetPasswordSuccessPage = lazy(() => import("../pages/ResetPasswordSuccessPage"));
const OAuthCallbackPage = lazy(() => import("../pages/OAuthCallbackPage"));
const HomePage = lazy(() => import("../pages/HomePage"));
const ProfilePage = lazy(() => import("../pages/ProfilePage"));
const ChangePasswordPage = lazy(() => import("../pages/ChangePasswordPage"));
const NotFoundPage = lazy(() => import("../pages/NotFoundPage"));

function RouteLoading() {
  return (
    <main className="auth-loading" aria-live="polite" aria-busy="true">
      <span className="auth-loading-spinner" aria-hidden="true" />
      <p>Đang tải...</p>
    </main>
  );
}

function AppRoutes() {
  return (
    <Suspense fallback={<RouteLoading />}>
      <Routes>
      {/* Trang chủ */}
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path={ROUTES.HOME} element={<HomePage />} />
        <Route path={ROUTES.PROFILE} element={<ProfilePage />} />
        <Route path={ROUTES.CHANGE_PASSWORD} element={<ChangePasswordPage />} />
      </Route>

      {/* ================= Authentication ================= */}

      <Route path={ROUTES.LOGIN} element={<LoginPage />} />

      <Route path={ROUTES.REGISTER} element={<RegisterPage />} />

      {/* ================= OAuth Callback ================= */}

      <Route
        path={ROUTES.OAUTH_GOOGLE_CALLBACK}
        element={<OAuthCallbackPage provider="google" />}
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

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
}

export default AppRoutes;
