import { Navigate, Route, Routes } from "react-router-dom";

import { ROUTES } from "../constants/routes";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import ForgotPasswordPage from "../pages/ForgotPasswordPage";
import EmailSentPage from "../pages/EmailSentPage";
import OTPPage from "../pages/OTPPage";
import ResetPasswordPage from "../pages/ResetPasswordPage";
import ResetPasswordSuccessPage from "../pages/ResetPasswordSuccessPage";

function AppRoutes() {
  return (
    <Routes>
      {/* Trang chủ - Chuyển hướng về trang đăng nhập */}
      <Route
        path={ROUTES.HOME}
        element={<Navigate to={ROUTES.LOGIN} replace />}
      />

      {/* Nhóm Authentication (Xác thực) */}
      <Route path={ROUTES.LOGIN} element={<LoginPage />} />
      <Route path={ROUTES.REGISTER} element={<RegisterPage />} />

      {/* Nhóm Quên mật khẩu (Theo luồng: Quên -> Gửi Email -> Nhập OTP -> Đổi mật khẩu -> Thành công) */}
      <Route path={ROUTES.FORGOT_PASSWORD} element={<ForgotPasswordPage />} />
      <Route path={ROUTES.EMAIL_SENT} element={<EmailSentPage />} />
      <Route path={ROUTES.OTP} element={<OTPPage />} />
      <Route path={ROUTES.RESET_PASSWORD} element={<ResetPasswordPage />} />
      <Route
        path={ROUTES.RESET_PASSWORD_SUCCESS}
        element={<ResetPasswordSuccessPage />}
      />

      {/* Trang 404 - Không tìm thấy trang */}
      <Route path="*" element={<h2>404 - Không tìm thấy trang</h2>} />
    </Routes>
  );
}

export default AppRoutes;
