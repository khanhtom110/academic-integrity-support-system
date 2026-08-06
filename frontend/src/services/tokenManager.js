/**
 * ==========================================================
 * Token Manager
 * ----------------------------------------------------------
 * Quản lý Access Token trong FE Memory.
 * Access Token sẽ mất khi refresh trình duyệt.
 * ==========================================================
 */

let accessToken = null;

/**
 * Lưu Access Token
 */
export function setAccessToken(token) {
  accessToken = token;
}

/**
 * Lấy Access Token
 */
export function getAccessToken() {
  return accessToken;
}

/**
 * Kiểm tra có Access Token không
 */
export function hasAccessToken() {
  return Boolean(accessToken);
}

/**
 * Xóa Access Token
 */
export function clearAccessToken() {
  accessToken = null;
}
