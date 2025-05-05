package com.example.soop.domain.user.req;

import com.example.soop.domain.user.type.Category;
import com.example.soop.domain.user.type.Language;
import com.example.soop.domain.user.type.Style;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ExpertSignupRequest(
    @NotBlank String providerId,
    @NotBlank String email,
    @NotBlank String nickname,

    @NotNull Category category,
    @NotNull Integer experience,
    @NotNull List<Style> styles,
    @NotNull Language language,
    @NotBlank String bio
) implements BaseSignupRequest {}