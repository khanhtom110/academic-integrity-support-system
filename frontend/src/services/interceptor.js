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

    console.log("===== REQUEST INTERCEPTOR =====");
    console.log("Access Token:", accessToken);

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

    // Không có response
    if (!error.response) {
      return Promise.reject(error);
    }

    // Không phải 401
    if (error.response.status !== 401) {
      return Promise.reject(error);
    }

    // Tránh lặp vô hạn
    if (originalRequest._retry) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const refreshToken = getRefreshToken();

      if (!refreshToken) {
        throw new Error("Refresh Token không tồn tại.");
      }

      /**
       * Gọi API Refresh Token
       */
      const response = await apiClient.post(API.AUTH.REFRESH_TOKEN, {
        refreshToken,
      });

      /**
       * Backend trả:
       * {
       *    accessToken,
       *    refreshToken,
       *    id,
       *    tokenType
       * }
       */
      const auth = response.data.data;

      /**
       * Lưu Access Token mới
       */
      setAccessToken(auth.accessToken);

      /**
       * Nếu Backend cấp Refresh Token mới
       */
      if (auth.refreshToken) {
        saveRefreshToken(auth.refreshToken);
      }

      /**
       * Gắn Access Token mới
       */
      originalRequest.headers.Authorization = `Bearer ${auth.accessToken}`;

      /**
       * Gửi lại Request cũ
       */
      return apiClient(originalRequest);
    } catch (err) {
      /**
       * Refresh Token hết hạn
       */

      clearAccessToken();

      removeRefreshToken();

      /**
       * Quay về Login
       */

      window.location.href = "/login";

      return Promise.reject(err);
    }
  },
);

export default apiClient;
