package com.example.soop.domain.user.res;

import com.example.soop.domain.user.User;

public record GeneralUserResponse(
    Long id,
    String providerId,
    String email,
    String nickName
) {

    public static GeneralUserResponse fromEntity(User user) {
        return new GeneralUserResponse(
            user.getId(),
            user.getProviderId(),
            user.getEmail(),
            user.getNickname()
        );
    }
}
