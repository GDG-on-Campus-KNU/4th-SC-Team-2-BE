package com.example.soop.global.exception;

import com.example.soop.global.code.ErrorCode;

public class UserException extends RuntimeException {

    private final ErrorCode code;

    public UserException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }
}