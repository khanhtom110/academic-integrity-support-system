const OAUTH_STATE_PREFIX = "oauth_state";
const DEVELOPMENT_ORIGIN = "http://localhost:5173";
const DEFAULT_CLIENT_IDS = {
  google:
    "930372998229-hvum0n94hf69l7u7po0vc5rncage449s.apps.googleusercontent.com",
  outlook: "f2fbd8d6-479a-4444-ae64-d40b9ebce928",
};

export const getAppOrigin = () => {
  const configuredOrigin = import.meta.env.VITE_APP_ORIGIN?.trim();
  const currentOrigin = typeof window === "undefined" ? "" : window.location.origin;

  return (configuredOrigin || currentOrigin || DEVELOPMENT_ORIGIN).replace(
    /\/$/,
    "",
  );
};

const providers = {
  google: {
    authorizationUrl: "https://accounts.google.com/o/oauth2/v2/auth",
    clientId:
      import.meta.env.VITE_GOOGLE_CLIENT_ID || DEFAULT_CLIENT_IDS.google,
    callbackPath: "/oauth2/callback/google",
    scope: "email profile",
  },
  outlook: {
    authorizationUrl:
      "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
    clientId:
      import.meta.env.VITE_OUTLOOK_CLIENT_ID || DEFAULT_CLIENT_IDS.outlook,
    callbackPath: "/oauth2/callback/outlook",
    scope: "openid profile email User.Read",
  },
};

function createState() {
  const randomBytes = new Uint8Array(32);
  window.crypto.getRandomValues(randomBytes);

  return Array.from(randomBytes, (byte) => byte.toString(16).padStart(2, "0")).join(
    "",
  );
}

function getStateKey(provider) {
  return `${OAUTH_STATE_PREFIX}:${provider}`;
}

export function createOAuthAuthorizationUrl(provider) {
  const config = providers[provider];

  if (!config) {
    throw new Error("Nhà cung cấp đăng nhập không hợp lệ.");
  }

  if (!config.clientId) {
    throw new Error(`Chưa cấu hình OAuth client ID cho ${provider}.`);
  }

  const state = createState();
  const redirectUri = `${getAppOrigin()}${config.callbackPath}`;
  const authorizationUrl = new URL(config.authorizationUrl);

  sessionStorage.setItem(getStateKey(provider), state);

  authorizationUrl.searchParams.set("client_id", config.clientId);
  authorizationUrl.searchParams.set("redirect_uri", redirectUri);
  authorizationUrl.searchParams.set("response_type", "code");
  authorizationUrl.searchParams.set("scope", config.scope);
  authorizationUrl.searchParams.set("state", state);

  return authorizationUrl.toString();
}

export function consumeOAuthState(provider, returnedState) {
  const stateKey = getStateKey(provider);
  const expectedState = sessionStorage.getItem(stateKey);

  sessionStorage.removeItem(stateKey);

  return Boolean(
    expectedState && returnedState && expectedState === returnedState,
  );
}
