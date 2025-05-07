package com.example.soop.domain.chat.dto.res;

import com.example.soop.domain.chat.type.EmpathyLevel;
import com.example.soop.domain.chat.type.ToneLevel;

public record AIChatRoomInfoResponse(
    Long id,
    String name,
    String description,
    EmpathyLevel empathyLevel,
    ToneLevel tone,
    int image
) {

}
