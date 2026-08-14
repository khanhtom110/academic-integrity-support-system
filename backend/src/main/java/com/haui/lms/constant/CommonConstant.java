package com.haui.lms.constant;

import java.util.Set;

public final class CommonConstant {
    public static final int USERNAME_LENGTH = 120;
    public static final int PASSWORD_LENGTH = 120;
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int FULLNAME_MIN_LENGTH = 4;
    public static final int FULLNAME_LENGTH = 120;
    public static final int PHONE_LENGTH = 15;
    public static final String PHONE_REGEX = "^0[35789]\\d{8}$";
    // Yeu cau it nhat 1 chu hoa, 1 chu thuong, 1 chu so, 1 ky tu dac biet (do dai kiem tra rieng qua @Size)
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).*$";
    public static final int EMAIL_LENGTH = 100;
    public static final int ADDRESS_LENGTH = 255;
    public static final int CONDITION_LENGTH = 1000;
    public static final String BEARER_TOKEN = "Bearer";
    public static final int BCRYPT_STRENGTH = 10;

    public static final String GOOGLE_PROVIDER = "google";

    public static final class User {
        public static final int AVATAR_LENGTH = 500;
        public static final int HOBBY_LENGTH = 300;
    }

    /**
     * Cau hinh dung chung cho moi chuc nang upload anh len Cloudinary. Module moi
     * chi can them mot hang so folder rieng o day.
     */
    public static final class Upload {
        private Upload() {
        }

        // 5MB: anh chup tu dien thoai thuong 2-5MB, dat thap hon se chan oan
        public static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

        public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

        // Anh duoc resize ve kich thuoc nay ngay khi upload de tiet kiem dung luong
        public static final int AVATAR_DIMENSION = 500;

        // Thu muc tren Cloudinary, tach theo tung module cho de quan ly
        public static final String AVATAR_FOLDER = "aiss/avatars";
    }

}
