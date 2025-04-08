package com.example.soop.global.exception;

import com.example.soop.global.code.ErrorCode;
import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {

    private final ErrorCode code;

    public JwtAuthenticationException(ErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }

}