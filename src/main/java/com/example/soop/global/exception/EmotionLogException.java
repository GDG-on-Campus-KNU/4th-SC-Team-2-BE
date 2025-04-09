package com.example.soop.global.exception;

import com.example.soop.global.code.ErrorCode;

public class EmotionLogException extends RuntimeException {

    private final ErrorCode code;

    public EmotionLogException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }
}