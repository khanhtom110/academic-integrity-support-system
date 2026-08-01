import "./AuthLayout.css";

import logo from "../../../../assets/images/logo.svg";

function AuthLayout({ children }) {
  return (
    <main className="auth-layout">
      <div className="auth-wrapper">
        <img
          src={logo}
          alt="Logo Đại học Công nghiệp Hà Nội"
          className="auth-logo"
        />

        <div className="auth-container">{children}</div>
      </div>
    </main>
  );
}

export default AuthLayout;
