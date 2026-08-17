export const API = {
  AUTH: {
    // LOGIN && REGISTER
    LOGIN: "/api/v1/auth/login",
    REGISTER: "/api/v1/auth/register",
    // OTP
    VERIFY_OTP: "/api/v1/auth/verify-otp",
    RESEND_OTP: "/api/v1/auth/resend-otp",
    FORGOT_PASSWORD: "/api/v1/auth/forgot-password",
    VERIFY_RESET_OTP: "/api/v1/auth/verify-reset-otp",
    RESET_PASSWORD: "/api/v1/auth/reset-password",
    // REFRESH_TOKEN
    REFRESH_TOKEN: "/api/v1/auth/refresh",
    // LOGIN: GOOGLE OUTLOOK
    GOOGLE_LOGIN: "/api/v1/auth/google",
    OUTLOOK_LOGIN: "/api/v1/auth/outlook",
  },

  USER: {
    LOGOUT: "/api/v1/user/logout",
  },
};
