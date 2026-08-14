package com.haui.lms.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String folder, String publicId, int dimension) {
        validateImage(file);

        try {
            Map<String, Object> options = ObjectUtils.asMap("public_id", folder + "/" + publicId, "resource_type",
                    "image",
                    // Ghi de anh cu cua chinh doi tuong nay thay vi tao them file moi
                    "overwrite", true,
                    // Xoa cache CDN de nguoi dung thay anh moi ngay lap tuc
                    "invalidate", true,
                    // Resize + nen ngay khi upload: anh luu tren Cloudinary chi con vai chuc KB.
                    // c_fill giu ty le va cat vua khung, g_auto tu chon vung quan trong cua anh,
                    // q_auto va f_auto de Cloudinary tu chon muc nen va dinh dang toi uu
                    "transformation", String.format("c_fill,g_auto,w_%d,h_%d,q_auto,f_auto", dimension, dimension));

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            return (String) result.get("secure_url");

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary. Folder: {}, PublicId: {}", folder, publicId, e);
            throw new AppException(500, ErrorMessage.UPLOAD_IMAGE_FAIL);
        }
    }

    @Override
    public void deleteImage(String folder, String publicId) {
        try {
            cloudinary.uploader().destroy(folder + "/" + publicId,
                    ObjectUtils.asMap("resource_type", "image", "invalidate", true));
        } catch (IOException e) {
            log.error("Failed to delete image on Cloudinary. Folder: {}, PublicId: {}", folder, publicId, e);
            throw new AppException(500, ErrorMessage.Upload.DELETE_IMAGE_FAIL);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(400, ErrorMessage.Upload.FILE_EMPTY);
        }

        if (file.getSize() > CommonConstant.Upload.MAX_IMAGE_SIZE) {
            throw new AppException(400, ErrorMessage.Upload.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null
                || !CommonConstant.Upload.ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new AppException(400, ErrorMessage.Upload.INVALID_IMAGE_TYPE);
        }
    }
}
