package com.example.soop.domain.chat.dto.req;

public record ChatRequest(
    Long chatRoomId, // 채팅방 ID
    String content  // 메시지 내용
) {

}
