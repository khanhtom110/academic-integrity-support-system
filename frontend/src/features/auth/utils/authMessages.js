const DEFAULT_ERROR_MESSAGES = {
  login: "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.",
  register: "Đăng ký thất bại. Vui lòng thử lại.",
  forgotPassword: "Không thể gửi mã OTP. Vui lòng thử lại.",
  verifyOtp: "Xác thực mã OTP thất bại.",
  resendOtp: "Không thể gửi lại mã OTP.",
  resetPassword: "Không thể đặt lại mật khẩu. Vui lòng thử lại.",
  oauth: "Đăng nhập bằng tài khoản bên thứ ba thất bại.",
};

const VIETNAMESE_MARKS = /[ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠƯẠ-ỹ]/i;
const COMMON_VIETNAMESE_WORDS =
  /\b(vui lòng|không|đăng nhập|đăng ký|mật khẩu|tài khoản|xác thực|mã otp|thất bại|thành công|hết hạn|đã tồn tại)\b/i;

function isVietnameseMessage(message) {
  return VIETNAMESE_MARKS.test(message) || COMMON_VIETNAMESE_WORDS.test(message);
}

function getRawMessage(error) {
  const message = error?.response?.data?.message;
  return typeof message === "string" ? message.trim() : "";
}

function includesAny(message, patterns) {
  return patterns.some((pattern) => message.includes(pattern));
}

export function getVietnameseAuthError(error, context, options = {}) {
  const rawMessage = getRawMessage(error);

  if (rawMessage && isVietnameseMessage(rawMessage)) {
    return rawMessage;
  }

  const message = rawMessage.toLowerCase();
  const status = error?.response?.status;

  if (!error?.response) {
    return "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại.";
  }

  if (
    includesAny(message, [
      "cannot convert authorization code to token",
      "cannot convert oath code",
      "invalid_grant",
      "authorization code has expired",
      "authorization code was already redeemed",
    ])
  ) {
    const startAgain = options.appOrigin
      ? ` Vui lòng bắt đầu lại từ ${options.appOrigin} và không sử dụng lại đường dẫn callback cũ.`
      : " Vui lòng bắt đầu lại quá trình đăng nhập và không sử dụng lại đường dẫn callback cũ.";

    return `Mã đăng nhập không thể đổi thành token.${startAgain}`;
  }

  if (
    includesAny(message, [
      "invalid credentials",
      "bad credentials",
      "incorrect password",
      "email or password",
      "login failed",
    ])
  ) {
    return "Email hoặc mật khẩu không chính xác.";
  }

  if (
    includesAny(message, [
      "email already exists",
      "email already registered",
      "already in use",
      "duplicate email",
      "account already exists",
    ])
  ) {
    return "Email này đã được sử dụng. Vui lòng đăng nhập hoặc dùng email khác.";
  }

  if (includesAny(message, ["user not found", "account not found"])) {
    return "Không tìm thấy tài khoản tương ứng.";
  }

  if (
    includesAny(message, [
      "account is not verified",
      "account not verified",
      "email is not verified",
      "email not verified",
    ])
  ) {
    return "Tài khoản chưa được xác thực. Vui lòng kiểm tra email để xác thực tài khoản.";
  }

  if (includesAny(message, ["otp expired", "expired otp", "code expired"])) {
    return "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.";
  }

  if (
    includesAny(message, [
      "invalid otp",
      "incorrect otp",
      "wrong otp",
      "otp does not match",
      "invalid verification code",
    ])
  ) {
    return "Mã OTP không chính xác. Vui lòng kiểm tra và nhập lại.";
  }

  if (
    includesAny(message, [
      "refresh token expired",
      "session expired",
      "token has expired",
      "invalid refresh token",
    ])
  ) {
    return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
  }

  if (status === 429) {
    return "Bạn đã thao tác quá nhiều lần. Vui lòng chờ một lát rồi thử lại.";
  }

  if (status >= 500) {
    return "Máy chủ đang gặp sự cố. Vui lòng thử lại sau.";
  }

  if (status === 403) {
    return "Bạn không có quyền thực hiện thao tác này.";
  }

  if (status === 401) {
    return context === "login"
      ? "Email hoặc mật khẩu không chính xác."
      : "Thông tin xác thực không hợp lệ hoặc đã hết hạn.";
  }

  if (status === 404) {
    return "Không tìm thấy tài khoản hoặc thông tin được yêu cầu.";
  }

  if (status === 409) {
    return context === "register"
      ? "Email này đã được sử dụng. Vui lòng đăng nhập hoặc dùng email khác."
      : "Thông tin đã tồn tại hoặc đang bị trùng lặp.";
  }

  return DEFAULT_ERROR_MESSAGES[context] || "Đã xảy ra lỗi. Vui lòng thử lại.";
}

export function getVietnameseSuccessMessage(message, fallback) {
  const normalizedMessage = typeof message === "string" ? message.trim() : "";

  if (normalizedMessage && isVietnameseMessage(normalizedMessage)) {
    return normalizedMessage;
  }

  const lowerMessage = normalizedMessage.toLowerCase();

  if (
    includesAny(lowerMessage, [
      "otp sent",
      "otp has been sent",
      "verification code sent",
      "resent successfully",
    ])
  ) {
    return "Mã OTP mới đã được gửi.";
  }

  return fallback;
}
