import React from "react";
import LoginForm from "../features/auth/components/LoginForm";
import AuthLayout from "../features/auth/components/AuthLayout";
const LoginPage = () => {
  return (
    <AuthLayout>
      <LoginForm></LoginForm>
    </AuthLayout>
  );
};

export default LoginPage;
