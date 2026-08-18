import { Link } from "react-router-dom";

import "../styles/pages/NotFoundPage.css";

import AuthLayout from "../features/auth/components/AuthLayout";
import Card from "../components/ui/Card";
import { ROUTES } from "../constants/routes";

function NotFoundPage() {
  return (
    <AuthLayout>
      <Card className="not-found-card">
        <p className="not-found-code" aria-hidden="true">404</p>
        <h1 className="heading-2">Không tìm thấy trang</h1>
        <p>Đường dẫn bạn truy cập không tồn tại hoặc đã được thay đổi.</p>
        <Link to={ROUTES.LOGIN} className="primary-btn not-found-link">
          Quay lại đăng nhập
        </Link>
      </Card>
    </AuthLayout>
  );
}

export default NotFoundPage;
