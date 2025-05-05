package com.example.soop.domain.user.res;

import com.example.soop.domain.user.ExpertProfile;
import com.example.soop.domain.user.User;
import com.example.soop.domain.user.UserType;
import com.example.soop.domain.user.type.Category;
import com.example.soop.domain.user.type.Language;
import com.example.soop.domain.user.type.Style;
import java.util.List;

public record ExpertUserResponse(
    Long id,
    String providerId,
    String email,
    String nickname,
    UserType userType,
    Category category,
    int experience,
    List<Style> styles,
    Language language,
    String bio
) {
    public static ExpertUserResponse fromEntity(User user) {
        ExpertProfile profile = user.getExpertProfile();
        return new ExpertUserResponse(
            user.getId(),
            user.getProviderId(),
            user.getEmail(),
            user.getNickname(),
            user.getUserType(),
            profile.getCategory(),
            profile.getExperience(),
            profile.getStyles(),
            profile.getLanguage(),
            profile.getBio()
        );
    }
}
