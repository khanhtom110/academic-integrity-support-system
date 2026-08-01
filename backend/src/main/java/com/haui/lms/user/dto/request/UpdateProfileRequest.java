package com.haui.lms.user.dto.request;

import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @Size(max = CommonConstant.FULLNAME_LENGTH, message = ErrorMessage.INVALID_FORMAT_FULLNAME) String fullName,

        String phone,

        @Size(max = CommonConstant.ADDRESS_LENGTH, message = ErrorMessage.INVALID_FORMAT_ADDRESS) String address) {
}
