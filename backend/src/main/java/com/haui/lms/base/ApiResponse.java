package com.haui.lms.base;

import java.time.Instant;

public record ApiResponse<T>(
        int statusCode,

        String message,

        T data,

        Instant timestamp
) {
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data, Instant.now());
    }
    public static <T> ApiResponse<T> ok(String message,T data){
        return new ApiResponse<>(200,message,data,Instant.now());
    }public static <T> ApiResponse<T> ok(T data){
        return new ApiResponse<>(200,null,data,Instant.now());
    }
    public static <T> ApiResponse<T>error(int code, String message){
        return new ApiResponse<T>(code,message,null,Instant.now());
    }
    public static <T> ApiResponse<T> error(int code, String message, T data){
        return new ApiResponse<>(code,message,data,Instant.now());
    }
}