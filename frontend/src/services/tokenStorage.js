/**
 * ==========================================================
 * Refresh Token Storage
 * ----------------------------------------------------------
 * Refresh Token được lưu trong sessionStorage để tồn tại qua F5
 * nhưng tự động bị xóa khi tab trình duyệt đóng.
 * ==========================================================
 */

const REFRESH_TOKEN_KEY = "refreshToken";

/**
 * Lưu Refresh Token
 */
export function saveRefreshToken(token) {
  sessionStorage.setItem(REFRESH_TOKEN_KEY, token);
}

/**
 * Lấy Refresh Token
 */
export function getRefreshToken() {
  return sessionStorage.getItem(REFRESH_TOKEN_KEY);
}

/**
 * Kiểm tra có Refresh Token không
 */
export function hasRefreshToken() {
  return Boolean(sessionStorage.getItem(REFRESH_TOKEN_KEY));
}

/**
 * Xóa Refresh Token
 */
export function removeRefreshToken() {
  sessionStorage.removeItem(REFRESH_TOKEN_KEY);
}
