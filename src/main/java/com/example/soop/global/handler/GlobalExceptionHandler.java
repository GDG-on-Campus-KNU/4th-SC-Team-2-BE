package com.example.soop.global.handler;

import com.example.soop.global.exception.EmotionLogException;
import com.example.soop.global.exception.RefreshTokenException;
import com.example.soop.global.exception.UserException;
import com.example.soop.global.format.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ApiResponse<Void> handleUserException(UserException e) {
        return ApiResponse.createFail(e.getErrorCode());
    }

    @ExceptionHandler(EmotionLogException.class)
    public ApiResponse<Void> EmotionLogException(EmotionLogException e) {
        return ApiResponse.createFail(e.getErrorCode());
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ApiResponse<Void> RefreshTokenException(RefreshTokenException e) {
        return ApiResponse.createFail(e.getErrorCode());
    }

}
