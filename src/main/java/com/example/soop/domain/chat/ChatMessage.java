package com.example.soop.domain.chat;

import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;
    private String roomId;
    private String sender; // "user" or "bot"
    private String message;
    private LocalDateTime createdAt = LocalDateTime.now();
}