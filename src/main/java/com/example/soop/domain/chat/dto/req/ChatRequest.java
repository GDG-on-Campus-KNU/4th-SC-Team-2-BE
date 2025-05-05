package com.example.soop.domain.chat.dto.req;

public record ChatRequest(
    Long chatRoomId, // 채팅방 ID
    Long targetUserId, // 수신자 ID(채팅방 생성 시 필요)
    String content  // 메시지 내용
) {

}
