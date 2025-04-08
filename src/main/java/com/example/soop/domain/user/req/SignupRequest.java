package com.example.soop.domain.user.req;

public record SignupRequest(
    String providerId,
    String email,
    String nickname
) {

}
