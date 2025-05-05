package com.example.soop.domain.user.res;

import com.example.soop.domain.user.User;

public record UserResponse(
    Long id,
    String providerId,
    String email,
    String nickName
) {

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getProviderId(),
            user.getEmail(),
            user.getNickname()
        );
    }
}
