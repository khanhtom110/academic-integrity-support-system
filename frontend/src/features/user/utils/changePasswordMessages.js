import { getUserError } from "./profile";

export function getChangePasswordError(error) {
  const message = error?.response?.data?.message;
  const normalized = typeof message === "string" ? message.toLowerCase() : "";

  if (normalized.includes("invalid password")) {
    return "Mật khẩu hiện tại không chính xác.";
  }

  if (normalized.includes("passwords do not match")) {
    return "Mật khẩu xác nhận không khớp.";
  }

  if (normalized.includes("different from the current password")) {
    return "Mật khẩu mới phải khác mật khẩu hiện tại.";
  }

  if (normalized.includes("8-120") || normalized.includes("password must")) {
    return "Mật khẩu phải từ 8–120 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.";
  }

  return getUserError(error, "Không thể đổi mật khẩu. Vui lòng thử lại.");
}
