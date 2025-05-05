package com.example.soop.domain.chat.dto.res;

import java.util.List;

public record ChatContentsResponse(
    List<ChatContentResponse> chats
) {

}
