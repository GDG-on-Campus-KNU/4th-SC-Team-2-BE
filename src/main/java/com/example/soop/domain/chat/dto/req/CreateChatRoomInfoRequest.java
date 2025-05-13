package com.example.soop.domain.chat.dto.req;

import com.example.soop.domain.chat.type.EmpathyLevel;
import com.example.soop.domain.chat.type.ToneLevel;

public record CreateChatRoomInfoRequest(
    String name,
    String description,
    EmpathyLevel empathyLevel,
    ToneLevel tone,
    int image
) {}