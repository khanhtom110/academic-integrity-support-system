package com.hit.comemyway.exception;

import tools.jackson.databind.exc.InvalidFormatException;
import com.hit.comemyway.base.ApiResponse;
import com.hit.comemyway.constant.ErrorMessage;
import com.hit.comemyway.exception.extended.AppException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
    return ResponseEntity.status(e.getErrorCode())
        .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
      String fieldName = fieldError.getField();
      String errorMessage = fieldError.getDefaultMessage();
      String errorCode = fieldError.getCode();

      if (!errors.containsKey(fieldName) || "NotBlank".equals(errorCode)) {
        errors.put(fieldName, errorMessage);
      }
    });

    return ResponseEntity.badRequest()
        .body(ApiResponse.error(400, ErrorMessage.BAD_REQUEST, errors));
  }

  @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
      BadCredentialsException ex) {
    return ResponseEntity.status(401)
        .body(ApiResponse.error(401, ErrorMessage.Auth.INVALID_CREDENTIALS));
  }

  @ExceptionHandler(org.springframework.security.authentication.InternalAuthenticationServiceException.class)
  public ResponseEntity<ApiResponse<Void>> handleInternalAuthenticationServiceException(
      InternalAuthenticationServiceException ex) {
    return ResponseEntity.status(401)
        .body(ApiResponse.error(401, ErrorMessage.Auth.INVALID_CREDENTIALS));
  }


  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException e) {
    return ResponseEntity.unprocessableContent()
        .body(ApiResponse.error(422, "Invalid parameter: " + e.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handlerHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    String customMessage = ErrorMessage.INVALID_JSON_FORMAT;

    Throwable cause = e.getCause();

    while (cause != null) {
      if (cause instanceof InvalidFormatException invalidFormatException) {

        if (invalidFormatException.getTargetType() != null
            && invalidFormatException.getTargetType().isEnum()) {

          if (!invalidFormatException.getPath().isEmpty()) {
            // Lấy tên trường bị sai
            String fieldName = invalidFormatException.getPath()
                .get(invalidFormatException.getPath().size() - 1).getPropertyName();
            // Lấy danh sách các giá trị Enum hợp lệ
            String allowedValues = java.util.Arrays
                .toString(invalidFormatException.getTargetType().getEnumConstants());

            customMessage = String.format("Invalid value for field '%s'. Accepted values are: %s",
                fieldName, allowedValues);
          }
        }
        break;
      }
      cause = cause.getCause();
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(400, customMessage));
  }

  @ExceptionHandler(value = IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handlingIllegalArgumentException(
      IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
    log.error("Lỗi hệ thống nghiêm trọng: ", ex);
    return ResponseEntity.status(500).body(ApiResponse.error(500, ErrorMessage.EXCEPTION_GENERAL));
  }
}
