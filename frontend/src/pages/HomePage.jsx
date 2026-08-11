import { useState } from "react";
import { useNavigate } from "react-router-dom";

import "./HomePage.css";

import AuthLayout from "../features/auth/components/AuthLayout";
import Button from "../components/ui/Button";
import Card from "../components/ui/Card";
import { ROUTES } from "../constants/routes";
import { useAuth } from "../hooks/useAuth";

function HomePage() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const handleLogout = async () => {
    setIsLoggingOut(true);

    try {
      await logout();
    } finally {
      navigate(ROUTES.LOGIN, { replace: true });
    }
  };

  return (
    <AuthLayout>
      <Card className="login-success">
        <span className="login-success-icon" aria-hidden="true">
          ✓
        </span>
        <h1 className="heading-2">Đăng nhập thành công</h1>
        <p>Bạn đã đăng nhập vào hệ thống xác thực học thuật.</p>
        <Button
          onClick={handleLogout}
          disabled={isLoggingOut}
          aria-busy={isLoggingOut}
        >
          {isLoggingOut ? "Đang đăng xuất..." : "Đăng xuất"}
        </Button>
      </Card>
    </AuthLayout>
  );
}

export default HomePage;
