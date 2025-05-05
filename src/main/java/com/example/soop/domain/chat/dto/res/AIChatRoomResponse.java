package com.example.soop.domain.chat.dto.res;

import com.example.soop.domain.chat.entity.RoomStatus;
import com.example.soop.domain.chat.type.EmpathyLevel;
import com.example.soop.domain.chat.type.ToneLevel;

public record AIChatRoomResponse(
    Long roomId,
    String botName,
    String description,
    EmpathyLevel empathyLevel,
    ToneLevel tone,
    String latestMessage,
    RoomStatus roomStatus
) {}
