import React from "react";
import LoginForm from "../features/auth/components/LoginForm/LoginForm";
import AuthLayout from "../features/auth/components/AuthLayout/AuthLayout";
const LoginPage = () => {
  return (
    <AuthLayout>
      <LoginForm></LoginForm>
    </AuthLayout>
  );
};

export default LoginPage;
