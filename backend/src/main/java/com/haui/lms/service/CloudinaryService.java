package com.haui.lms.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    /**
     * Upload anh len Cloudinary va tra ve URL cua anh.
     * <p>
     * publicId duoc dat co dinh theo doi tuong (vi du theo userId) nen lan upload
     * sau se ghi de len anh cu, khong sinh rac. URL tra ve co kem so version nen
     * trinh duyet tu dong nhan anh moi, khong bi cache anh cu.
     *
     * @param file
     *            file anh nguoi dung gui len
     * @param folder
     *            thu muc tren Cloudinary, xem CommonConstant.Upload
     * @param publicId
     *            dinh danh cua anh trong folder do
     * @param dimension
     *            anh se duoc resize ve kich thuoc vuong nay truoc khi luu
     * @return URL https cua anh vua upload
     */
    String uploadImage(MultipartFile file, String folder, String publicId, int dimension);

    /**
     * Xoa anh khoi Cloudinary.
     *
     * @param folder
     *            thu muc chua anh
     * @param publicId
     *            dinh danh cua anh trong folder do
     */
    void deleteImage(String folder, String publicId);
}
