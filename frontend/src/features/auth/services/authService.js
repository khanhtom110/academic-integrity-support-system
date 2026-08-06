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

/**
 * API này sẽ được Interceptor sử dụng ở Bước 10.
 *
 * Hiện tại vẫn dùng apiClient.
 * Sang Bước 10 sẽ chuyển sang refreshClient.
 */
export const refreshToken = async (refreshToken) => {
  const response = await apiClient.post(API.AUTH.REFRESH_TOKEN, {
    refreshToken,
  });

  return response.data;
};

export const logout = async () => {
  const response = await apiClient.post(API.USER.LOGOUT);

  return response.data;
};

export const loginWithGoogle = (token) =>
  apiClient.post(API.AUTH.GOOGLE_LOGIN, token);

export const loginWithFacebook = (token) =>
  apiClient.post(API.AUTH.FACEBOOK_LOGIN, token);

export const loginWithOutlook = (token) =>
  apiClient.post(API.AUTH.OUTLOOK_LOGIN, token);
