const AVATAR_TYPES = new Set(["image/jpeg", "image/png", "image/webp"]);

export const MAX_AVATAR_SIZE = 5 * 1024 * 1024;
export const AVATAR_ACCEPT = [...AVATAR_TYPES].join(",");

export function validateAvatar(file) {
  if (!file) {
    return "Vui lòng chọn ảnh đại diện.";
  }

  if (!AVATAR_TYPES.has(file.type)) {
    return "Ảnh đại diện phải có định dạng JPEG, PNG hoặc WebP.";
  }

  if (file.size > MAX_AVATAR_SIZE) {
    return "Ảnh đại diện không được vượt quá 5 MB.";
  }

  return "";
}

export function resolveAvatarUrl(avatar) {
  if (!avatar || typeof avatar !== "string") {
    return "";
  }

  const normalized = avatar.trim();

  if (!normalized) {
    return "";
  }

  if (/^(?:https?:|data:|blob:)/i.test(normalized)) {
    return normalized;
  }

  const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/$/, "");
  const avatarPath = normalized.startsWith("/") ? normalized : `/${normalized}`;

  return `${apiBaseUrl}${avatarPath}`;
}

export function getUserError(error, fallback) {
  if (!error?.response) {
    return "Không thể kết nối đến máy chủ. Vui lòng thử lại.";
  }

  const message = error.response.data?.message;

  if (typeof message === "string" && message.trim()) {
    return message.trim();
  }

  if (error.response.status === 413) {
    return "Ảnh đại diện không được vượt quá 5 MB.";
  }

  if (error.response.status >= 500) {
    return "Máy chủ đang gặp sự cố. Vui lòng thử lại sau.";
  }

  return fallback;
}
