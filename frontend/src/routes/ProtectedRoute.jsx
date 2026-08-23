import { Navigate } from "react-router-dom";

import { ROUTES } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";

function ProtectedRoute({ children }) {
  const { authStatus, isAuthenticated } = useAuth();

  if (authStatus === "loading") {
    return (
      <main className="auth-loading" aria-live="polite" aria-busy="true">
        <span className="auth-loading-spinner" aria-hidden="true" />
        <p>Đang kiểm tra phiên đăng nhập...</p>
      </main>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  return children;
}

export default ProtectedRoute;
