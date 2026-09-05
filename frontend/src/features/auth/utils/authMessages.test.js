import { describe, expect, it } from "vitest";

import {
  getVietnameseAuthError,
  getVietnameseSuccessMessage,
} from "./authMessages";

function createApiError(status, message) {
  return { response: { status, data: { message } } };
}

describe("chuẩn hóa thông báo xác thực", () => {
  it("giữ nguyên thông báo tiếng Việt từ máy chủ", () => {
    const error = createApiError(400, "Mã OTP đã hết hạn.");

    expect(getVietnameseAuthError(error, "verifyOtp")).toBe(
      "Mã OTP đã hết hạn.",
    );
  });

  it("dịch lỗi đăng nhập phổ biến", () => {
    const error = createApiError(401, "Bad credentials");

    expect(getVietnameseAuthError(error, "login")).toBe(
      "Email hoặc mật khẩu không chính xác.",
    );
  });

  it("dịch lỗi mã OTP hết hạn", () => {
    const error = createApiError(400, "OTP expired");

    expect(getVietnameseAuthError(error, "verifyOtp")).toBe(
      "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.",
    );
  });

  it("không để lộ thông báo tiếng Anh không xác định", () => {
    const error = createApiError(400, "Unexpected authentication problem");

    expect(getVietnameseAuthError(error, "register")).toBe(
      "Đăng ký thất bại. Vui lòng thử lại.",
    );
  });

  it("hiển thị thông báo kết nối khi không nhận được phản hồi", () => {
    expect(getVietnameseAuthError(new Error("Network Error"), "login")).toBe(
      "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại.",
    );
  });

  it("dịch lỗi đổi mã OAuth thành token", () => {
    const error = createApiError(
      400,
      "Cannot convert Authorization Code to Token",
    );

    expect(
      getVietnameseAuthError(error, "oauth", {
        appOrigin: "https://example.com",
      }),
    ).toBe(
      "Mã đăng nhập không thể đổi thành token. Vui lòng bắt đầu lại từ https://example.com và không sử dụng lại đường dẫn callback cũ.",
    );
  });

  it("dịch thông báo gửi lại mã OTP thành công", () => {
    expect(
      getVietnameseSuccessMessage(
        "OTP sent successfully",
        "Mã OTP mới đã được gửi.",
      ),
    ).toBe("Mã OTP mới đã được gửi.");
  });

  it.each([
    [400, "login"],
    [401, "login"],
    [403, "oauth"],
    [404, "forgotPassword"],
    [409, "register"],
    [429, "verifyOtp"],
    [500, "resetPassword"],
  ])("không hiển thị tiếng Anh cho HTTP %s", (status, context) => {
    const result = getVietnameseAuthError(
      createApiError(status, "Unknown backend error"),
      context,
    );

    expect(result).not.toContain("Unknown backend error");
    expect(result).toMatch(/[À-ỹ]/i);
  });
});
