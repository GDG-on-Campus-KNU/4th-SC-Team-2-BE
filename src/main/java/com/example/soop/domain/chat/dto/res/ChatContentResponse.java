package com.example.soop.domain.chat.dto.res;

import java.time.LocalDateTime;

public record ChatContentResponse(
    String chatId,
    Long chatRoomId,
    Long senderId,
    String content,
    LocalDateTime createdAt
) {
}
