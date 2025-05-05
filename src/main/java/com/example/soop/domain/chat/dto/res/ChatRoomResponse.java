package com.example.soop.domain.chat.dto.res;

import com.example.soop.domain.chat.entity.RoomStatus;

public record ChatRoomResponse(
    Long chatRoomId,
    Long targetUserId,
    String targetUserEmail, // 상대방 이메일
    String targetUserNickname,// 상대방 닉네임
    String title, //채팅방 제목
    String latestContent, // 가장 최근 대화
    Boolean isNew, // 내가 읽지 않은 새 대화 있는지 여부
    RoomStatus status
) {

}
