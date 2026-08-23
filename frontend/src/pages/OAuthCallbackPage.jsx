import AuthLayout from "../features/auth/components/AuthLayout";
import OAuthCallback from "../features/auth/components/OAuthCallback";

function OAuthCallbackPage({ provider }) {
  return (
    <AuthLayout>
      <OAuthCallback provider={provider} />
    </AuthLayout>
  );
}

export default OAuthCallbackPage;
