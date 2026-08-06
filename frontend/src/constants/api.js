export const API = {
  AUTH: {
    // LOGIN && REGISTER
    LOGIN: "/api/v1/auth/login",
    REGISTER: "/api/v1/auth/register",
    // OTP
    VERIFY_OTP: "/api/v1/auth/verify-otp",
    RESEND_OTP: "/api/v1/auth/resend-otp",
    // REFRESH_TOKEN
    REFRESH_TOKEN: "/api/v1/auth/refresh",
    // LOGIN: GOOGLE FACEBOOK OUTLOOK
    GOOGLE_LOGIN: "/api/v1/auth/google",
    FACEBOOK_LOGIN: "/api/v1/auth/facebook",
    OUTLOOK_LOGIN: "/api/v1/auth/outlook",
  },

  USER: {
    LOGOUT: "/api/v1/user/logout",
  },
};
