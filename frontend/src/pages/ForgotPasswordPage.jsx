import React from "react";
import AuthLayout from "../features/auth/components/AuthLayout";
import ForgotPasswordForm from "../features/auth/components/ForgotPasswordForm";
const ForgotPasswordPage = () => {
  return (
    <AuthLayout>
      <ForgotPasswordForm />
    </AuthLayout>
  );
};

export default ForgotPasswordPage;
