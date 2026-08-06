/**
 * ==========================================================
 * Refresh Token Storage
 * ----------------------------------------------------------
 * Refresh Token được lưu trong localStorage.
 * ==========================================================
 */

const REFRESH_TOKEN_KEY = "refreshToken";

/**
 * Lưu Refresh Token
 */
export function saveRefreshToken(token) {
  localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

/**
 * Lấy Refresh Token
 */
export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

/**
 * Kiểm tra có Refresh Token không
 */
export function hasRefreshToken() {
  return Boolean(localStorage.getItem(REFRESH_TOKEN_KEY));
}

/**
 * Xóa Refresh Token
 */
export function removeRefreshToken() {
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}
