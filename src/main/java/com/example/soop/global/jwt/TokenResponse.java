package com.example.soop.global.jwt;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {

}
