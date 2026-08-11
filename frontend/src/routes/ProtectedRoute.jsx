import { Navigate } from "react-router-dom";

import { ROUTES } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.LOGIN} replace />;
  }

  return children;
}

export default ProtectedRoute;
