import apiClient from "./apiClient";
import { API } from "../constants/api";

import {
  getAccessToken,
  setAccessToken,
  clearAccessToken,
} from "./tokenManager";

import {
  getRefreshToken,
  saveRefreshToken,
  removeRefreshToken,
} from "./tokenStorage";

/**
 * ==========================================================
 * REQUEST INTERCEPTOR
 * ==========================================================
 */
apiClient.interceptors.request.use(
  (config) => {
    const accessToken = getAccessToken();

    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => Promise.reject(error),
);

/**
 * ==========================================================
 * RESPONSE INTERCEPTOR
 * ==========================================================
 */
apiClient.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    /**
     * Không có response
     */
    if (!error.response) {
      return Promise.reject(error);
    }

    /**
     * Không refresh đối với các API public
     */
    const publicApis = [
      API.AUTH.LOGIN,
      API.AUTH.REGISTER,
      API.AUTH.VERIFY_OTP,
      API.AUTH.RESEND_OTP,
      API.AUTH.FORGOT_PASSWORD,
      API.AUTH.VERIFY_RESET_OTP,
      API.AUTH.RESET_PASSWORD,
      API.AUTH.REFRESH_TOKEN,
      API.AUTH.GOOGLE_LOGIN,
      API.AUTH.FACEBOOK_LOGIN,
      API.AUTH.OUTLOOK_LOGIN,
    ];

    if (publicApis.includes(originalRequest.url)) {
      return Promise.reject(error);
    }

    /**
     * Không phải 401
     */
    if (error.response.status !== 401) {
      return Promise.reject(error);
    }

    /**
     * Tránh refresh nhiều lần
     */
    if (originalRequest._retry) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const refreshToken = getRefreshToken();

      if (!refreshToken) {
        throw new Error("Refresh Token không tồn tại.");
      }

      const response = await apiClient.post(API.AUTH.REFRESH_TOKEN, {
        refreshToken,
      });

      const auth = response.data.data;

      /**
       * Lưu Access Token mới
       */
      setAccessToken(auth.accessToken);

      /**
       * Backend có cấp Refresh Token mới
       */
      if (auth.refreshToken) {
        saveRefreshToken(auth.refreshToken);
      }

      /**
       * Gắn Access Token mới
       */
      originalRequest.headers.Authorization = `Bearer ${auth.accessToken}`;

      /**
       * Gửi lại request cũ
       */
      return apiClient(originalRequest);
    } catch (err) {
      clearAccessToken();

      removeRefreshToken();

      window.location.href = "/login";

      return Promise.reject(err);
    }
  },
);

export default apiClient;
