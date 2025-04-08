package com.example.soop.global.format;

import com.example.soop.global.code.ErrorCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private boolean isSuccess;
    private String code;
    private T data;
    private String message;

    public static <T> ApiResponse<T> createSuccess() {
        return new ApiResponse<>(true);
    }

    public static <T> ApiResponse<T> createSuccess(String message) {
        return new ApiResponse<>(true, message);
    }

    public static <T> ApiResponse<T> createSuccessWithData(T data) {
        return new ApiResponse<>(true, data);
    }

    public static <T> ApiResponse<T> createSuccessWithData(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static <T> ApiResponse<T> createFail(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> ApiResponse<T> createAuthFail(String message) {
        return new ApiResponse<>(false, "JWT401", message);
    }

    private ApiResponse(Boolean isSuccess) {
        this.isSuccess = isSuccess;
        this.code = "COMMON200";
        this.data = null;
        this.message = null;
    }

    private ApiResponse(Boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.code = "COMMON200";
        this.data = null;
        this.message = message;
    }

    private ApiResponse(Boolean isSuccess, T data) {
        this.isSuccess = isSuccess;
        this.code = "COMMON200";
        this.data = data;
        this.message = null;
    }

    private ApiResponse(Boolean isSuccess, T data, String message) {
        this.isSuccess = isSuccess;
        this.code = "COMMON200";
        this.data = data;
        this.message = message;
    }

    private ApiResponse(Boolean isSuccess, String code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.data = null;
        this.message = message;
    }

}