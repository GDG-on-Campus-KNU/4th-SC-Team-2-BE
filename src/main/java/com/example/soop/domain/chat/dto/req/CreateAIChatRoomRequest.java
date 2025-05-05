package com.example.soop.domain.chat.dto.req;

import com.example.soop.domain.chat.type.EmpathyLevel;
import com.example.soop.domain.chat.type.ToneLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAIChatRoomRequest(
    @NotBlank String name,
    @NotBlank String description,
    @NotNull EmpathyLevel empathyLevel,
    @NotNull ToneLevel tone
) {

}
