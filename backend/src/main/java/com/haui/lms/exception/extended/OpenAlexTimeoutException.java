package com.haui.lms.exception.extended;

/**
 * OpenAlex tra 504 khi mot truy van quet khoang thoi gian qua rong.
 * <p>
 * Day khong phai loi that su: chia nho khoang ngay roi goi lai thi thuong qua duoc. Tach thanh exception rieng de tang
 * phan tich biet duong chia nho va thu lai, thay vi bao that bai cho nguoi dung.
 */
public class OpenAlexTimeoutException extends RuntimeException {

    public OpenAlexTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
