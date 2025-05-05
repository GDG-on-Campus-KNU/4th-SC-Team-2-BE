package com.example.soop.domain.user.req;

import jakarta.validation.constraints.NotBlank;

public record GeneralSignupRequest(
    @NotBlank String providerId,
    @NotBlank String email,
    @NotBlank String nickname
) implements BaseSignupRequest {

}
