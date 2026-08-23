import AuthLayout from "../features/auth/components/AuthLayout";
import RegisterForm from "../features/auth/components/RegisterForm";

function RegisterPage() {
  return (
    <AuthLayout>
      <RegisterForm />
    </AuthLayout>
  );
}

export default RegisterPage;
