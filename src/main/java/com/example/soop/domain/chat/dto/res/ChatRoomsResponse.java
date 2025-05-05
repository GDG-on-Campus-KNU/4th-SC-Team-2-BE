package com.example.soop.domain.chat.dto.res;

import java.util.List;

public record ChatRoomsResponse(
    List<ChatRoomResponse> chatRooms
) {

}
