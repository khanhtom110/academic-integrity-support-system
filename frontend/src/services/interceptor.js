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

let refreshPromise = null;
let isRedirectingToLogin = false;

const publicApis = new Set([
  API.AUTH.LOGIN,
  API.AUTH.REGISTER,
  API.AUTH.VERIFY_OTP,
  API.AUTH.RESEND_OTP,
  API.AUTH.FORGOT_PASSWORD,
  API.AUTH.VERIFY_RESET_OTP,
  API.AUTH.RESET_PASSWORD,
  API.AUTH.REFRESH_TOKEN,
  API.AUTH.GOOGLE_LOGIN,
  API.AUTH.OUTLOOK_LOGIN,
]);

function getRequestPath(url = "") {
  try {
    return new URL(url, window.location.origin).pathname;
  } catch {
    return url.split("?")[0];
  }
}

function clearSessionAndRedirect() {
  clearAccessToken();
  removeRefreshToken();

  if (!isRedirectingToLogin && window.location.pathname !== "/login") {
    isRedirectingToLogin = true;
    window.location.replace("/login");
  }
}

async function refreshAccessToken() {
  const storedRefreshToken = getRefreshToken();

  if (!storedRefreshToken) {
    throw new Error("Không tìm thấy refresh token.");
  }

  const response = await apiClient.post(API.AUTH.REFRESH_TOKEN, {
    refreshToken: storedRefreshToken,
  });
  const auth = response.data?.data;

  if (!auth?.accessToken) {
    throw new Error("Phản hồi làm mới phiên không hợp lệ.");
  }

  setAccessToken(auth.accessToken);

  if (auth.refreshToken) {
    saveRefreshToken(auth.refreshToken);
  }

  return auth.accessToken;
}

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
    if (!originalRequest || publicApis.has(getRequestPath(originalRequest.url))) {
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
      if (!refreshPromise) {
        refreshPromise = refreshAccessToken().finally(() => {
          refreshPromise = null;
        });
      }

      const newAccessToken = await refreshPromise;

      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

      /**
       * Gửi lại request cũ
       */
      return apiClient(originalRequest);
    } catch (err) {
      clearSessionAndRedirect();

      return Promise.reject(err);
    }
  },
);

export default apiClient;
