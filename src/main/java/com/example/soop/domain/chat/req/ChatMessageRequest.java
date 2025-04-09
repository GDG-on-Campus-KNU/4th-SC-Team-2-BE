package com.example.soop.domain.chat.req;

public record ChatMessageRequest(
    String roomId,
    String sender, // "user" or "bot"
    String message
) {

}
