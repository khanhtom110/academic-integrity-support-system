package com.haui.lms.constant;

public final class ErrorMessage {
    private ErrorMessage() {
    }

    // General System Errors
    public static final String EXCEPTION_GENERAL = "An unexpected system error occurred. Please try again later.";
    public static final String UNAUTHORIZED = "You are not authorized to access this resource. Please log in.";
    public static final String FORBIDDEN = "Access to this resource is forbidden.";
    public static final String BAD_REQUEST = "The request is invalid or malformed.";
    public static final String FORBIDDEN_UPDATE_DELETE = "You do not have permission to update or delete this resource.";
    public static final String UPLOAD_IMAGE_FAIL = "Failed to upload the image. Please check the file and try again.";

    // Common DTO Validation Errors
    public static final String INVALID_JSON_FORMAT = "Invalid request body format or malformed JSON.";
    public static final String INVALID_SOME_THING_FIELD = "One or more fields contain invalid data.";
    public static final String INVALID_FORMAT_SOME_THING_FIELD = "The field format is invalid.";
    public static final String INVALID_SOME_THING_FIELD_IS_REQUIRED = "This field is required and cannot be empty.";
    public static final String NOT_BLANK_FIELD = "This field cannot be blank.";
    public static final String INVALID_FORMAT_USERNAME = "Username must be 4-120 characters long and contain only letters, numbers, and underscores.";
    public static final String INVALID_FORMAT_FULLNAME = "Fullname must be 4-120 characters long.";
    public static final String INVALID_FORMAT_PHONE = "Invalid phone number format";
    public static final String INVALID_FORMAT_ADDRESS = "Address must not exceed 255 characters";
    public static final String INVALID_FORMAT_PASSWORD = "Password must be 8-120 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";
    public static final String INVALID_USERNAME_LENGTH = "Username length is invalid. It must be between 4 and 120 characters.";
    public static final String INVALID_FORMAT_EMAIL = "Please enter a valid email address.";
    public static final String INVALID_FORMAT_CONDITION = "Condition must be under 1000 characters long.";
    public static final String INVALID_PARAMETER = "The provided parameter is invalid";
    public static final String PASSWORD_MISMATCH = "Passwords do not match.";

    // Authentication Errors (Auth)
    public static final class Auth {
        private Auth() {
        }

        public static final String INVALID_CREDENTIALS = "Invalid username, email, or password.";
        public static final String INVALID_PASSWORD = "Invalid password.";
        public static final String INVALID_REFRESH_TOKEN = "The refresh token is invalid or has expired.";
        public static final String EXPIRED_REFRESH_TOKEN = "Your session has expired. Please log in again.";
        public static final String LOGIN_FAIL = "Login failed. Please check your credentials and try again.";
        public static final String GET_TOKEN_CLAIM_SET_FAIL = "Failed to verify user session details.";
        public static final String TOKEN_INVALIDATED = "This session has been invalidated.";
        public static final String MALFORMED_TOKEN = "The authentication token is malformed.";
        public static final String TOKEN_ALREADY_INVALIDATED = "This session is already logged out.";
        public static final String INVALID_LOGOUT_TOKEN = "Invalid token provided for logout.";

        public static final String OTP_EXPIRED = "The OTP has expired. Please request a new one.";
        public static final String INVALID_OTP = "The provided OTP is invalid. Please check and try again.";
        public static final String INVALID_FORMAT_OTP = "OTP must contain exactly 6 digits.";
        public static final String RESET_SESSION_EXPIRED = "Your password reset session has expired. Please start the process again.";
        public static final String SEND_MAIL_FAIL = "Failed to send the email. Please try again later.";
        public static final String SESSION_EXPIRED = "Session expired";
        public static final String PASSWORD_SAME_AS_OLD = "New password must be different from the current password.";

        public static final String CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL = "Cannot convert Authorization Code to Token";
        public static final String CANNOT_CONVERT_OAUTH_CODE = "Cannot convert Oath code";
        public static final String CANNOT_FETCH_OAUTH_USER_INFO = "Cannot fetch User infor";
        public static final String FACEBOOK_EMAIL_MISSING = "Your Facebook account is missing email";
        public static final String GOOGLE_EMAIL_MISSING = "Your Google account is missing email";
        public static final String OUTLOOK_EMAIL_MISSING = "Your Outlook account is missing email";

    }

    // Journal Errors
    public static final class Journal {
        private Journal() {
        }

        public static final String INVALID_ISSN_FORMAT = "Invalid ISSN format. Expected 8 characters like 0092-8674.";
        public static final String JOURNAL_NOT_FOUND = "This journal was not found in the OpenAlex database. It does not necessarily mean the journal does not exist or is untrustworthy: many local journals are not indexed internationally.";
        public static final String OPENALEX_UNAVAILABLE = "Cannot reach the OpenAlex data source at the moment. Please try again later.";
        public static final String SEARCH_QUERY_TOO_SHORT = "Please enter at least 2 characters to search.";
    }

    // Upload Errors
    public static final class Upload {
        private Upload() {
        }

        public static final String FILE_EMPTY = "The uploaded file is empty. Please choose an image.";
        public static final String FILE_TOO_LARGE = "The image is too large. Maximum allowed size is 5MB.";
        public static final String INVALID_IMAGE_TYPE = "Unsupported image format. Only JPEG, PNG and WebP are allowed.";
        public static final String DELETE_IMAGE_FAIL = "Failed to delete the image. Please try again later.";
    }

    // User Errors
    public static final class User {
        private User() {
        }

        public static final String USER_NOT_EXISTED = "User does not exist.";
        public static final String AVATAR_NOT_EXISTED = "You do not have an avatar to remove.";
        public static final String USERNAME_EXISTED = "Username already exists. Please choose a different one.";
        public static final String EMAIL_EXISTED = "Email already exists. Please use another email or log in.";
        public static final String EMAIL_NOT_EXISTED = "Email does not exists.";
    }

    // Administrator Errors (Admin)
    public static final class Admin {
        private Admin() {
        }

        public static final String NOT_ADMIN = "You do not have administrator privileges to perform this action.";
    }
}
