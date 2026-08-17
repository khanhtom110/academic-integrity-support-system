import { API } from "../../../constants/api";
import apiClient from "../../../services/apiClient";

/**
 * ============================================================
 * Authentication Service
 * ============================================================
 */

export const login = async (data) => {
  const response = await apiClient.post(API.AUTH.LOGIN, data);

  return response.data;
};

export const register = async (data) => {
  const response = await apiClient.post(API.AUTH.REGISTER, data);

  return response.data;
};

export const verifyOtp = async (data) => {
  const response = await apiClient.post(API.AUTH.VERIFY_OTP, data);

  return response.data;
};

export const resendOtp = async (data) => {
  const response = await apiClient.post(API.AUTH.RESEND_OTP, data);

  return response.data;
};

export const forgotPassword = async (data) => {
  const response = await apiClient.post(API.AUTH.FORGOT_PASSWORD, data);

  return response.data;
};

export const verifyResetOtp = async (data) => {
  const response = await apiClient.post(API.AUTH.VERIFY_RESET_OTP, data);

  return response.data;
};

export const resetPassword = async (data) => {
  const response = await apiClient.post(API.AUTH.RESET_PASSWORD, data);

  return response.data;
};

/**
 * Refresh Token
 */
export const refreshToken = async (refreshToken) => {
  const response = await apiClient.post(API.AUTH.REFRESH_TOKEN, {
    refreshToken,
  });

  return response.data;
};

/**
 * Logout
 */
export const logout = async () => {
  const response = await apiClient.post(API.USER.LOGOUT);

  return response.data;
};

/**
 * ============================================================
 * OAuth Login
 * ============================================================
 */

const exchangeOAuthCode = async (endpoint, code) => {
  const response = await apiClient.post(endpoint, { code });

  return response.data;
};

export const loginWithGoogle = (code) =>
  exchangeOAuthCode(API.AUTH.GOOGLE_LOGIN, code);

export const loginWithOutlook = (code) =>
  exchangeOAuthCode(API.AUTH.OUTLOOK_LOGIN, code);
