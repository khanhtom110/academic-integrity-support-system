package com.haui.lms.constant;

public class UrlConstant {
    public static class Public {
        private static final String PREFIX = "/public";

        private Public() {
        }
    }

    public static class Auth {
        private static final String PREFIX = "/auth";

        // Thuoc pham vi cua Dieu (feature/be-auth-otp-redis) - de nguyen nhu
        // scaffold ban dau, chua tu y sua vi ban chua hoi lai Dieu.
        public static final String LOGIN = PREFIX + "/login";
        public static final String REGISTER = PREFIX + "/register";
        public static final String REFRESH_TOKEN = PREFIX + "/refresh";
        public static final String VERIFY_OTP = PREFIX + "/verify-otp";
        public static final String VERIFY_REGISTER_OTP = PREFIX + "/verify-register";

        // Thuoc pham vi cua minh (feature/be-user-management) - khop dung 2
        // endpoint lead da chot: request + confirm (confirm gop verify OTP va
        // doi mat khau lam 1 buoc, khong co buoc verify-otp rieng).
        private static final String FORGOT_PASSWORD_PREFIX = PREFIX + "/forgot-password";
        public static final String FORGOT_PASSWORD_REQUEST = FORGOT_PASSWORD_PREFIX + "/request";
        public static final String FORGOT_PASSWORD_CONFIRM = FORGOT_PASSWORD_PREFIX + "/confirm";

        private Auth() {
        }
    }

    public static class User {
        // Doi tu "/user" -> "/users" va "/profile" -> "/me" de khop dung
        // /api/v1/users/me va /api/v1/users/change-password nhu phan cong tuan 1
        // yeu cau (ban goc dung "/user" so, khong khop task).
        private static final String PREFIX = "/users";

        public static final String GET_PROFILE = PREFIX + "/me";
        public static final String CHANGE_PASSWORD = PREFIX + "/change-password";
        public static final String LOGOUT = PREFIX + "/logout";
        public static final String BOOK_APPOINTMENT = PREFIX + "/appointments/book";
        public static final String DEVICE_TOKEN = PREFIX + "/device-token";
        public static final String USER_REMINDER = PREFIX + "/user-reminder";

        private User() {
        }
    }

    public static class Admin {
        private static final String PREFIX = "/admin";

        public static final String GET_USER = PREFIX + "/user/{userId}";
        public static final String CREATE_USER = PREFIX + "/create-user";
        public static final String UPDATE_USER = PREFIX + "/update-user/{userId}";
        public static final String DELETE_USER = PREFIX + "/delete-user/{userId}";

        private Admin() {
        }
    }

    public static class Media {
        private static final String PREFIX = "/media";

        public static final String UPLOAD = PREFIX + "/upload";

        private Media() {
        }
    }
}
