package com.example.soop.domain.user.req;

public record LoginRequest(
    String providerId,
    String email
) {

}
