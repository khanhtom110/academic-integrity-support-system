package com.haui.lms.dto.response;

public record FacebookUserInfoResponse(
        // @formatter:off
        String id, String name, String email, PictureData picture) {
    public record PictureData(Data data) {
    }

    public record Data(String url) {
    }

    // Helper method tiện lợi để lấy URL ảnh đại diện ra dùng ngay
    public String getAvatarUrl() {
        if (picture != null && picture.data() != null) {
            return picture.data().url();
        }
        return null;
    }
}