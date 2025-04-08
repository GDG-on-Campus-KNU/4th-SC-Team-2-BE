package com.example.soop.global.exception;


import com.example.soop.global.code.ErrorCode;

public class RefreshTokenException extends RuntimeException {

    private final ErrorCode code;

    public RefreshTokenException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }
}