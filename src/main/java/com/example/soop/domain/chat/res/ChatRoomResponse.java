package com.example.soop.domain.chat.res;

import com.example.soop.domain.chat.ChatRoom;

public record ChatRoomResponse(
    String roomId
) {

    public static ChatRoomResponse fromEntity(ChatRoom savedChatRoom) {
        return new ChatRoomResponse(savedChatRoom.getId());
    }
}
