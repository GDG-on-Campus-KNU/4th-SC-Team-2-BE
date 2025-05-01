package com.example.soop.domain.chat;

import com.example.soop.global.entity.MongoBaseEntity;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "chat_rooms")
public class ChatRoom extends MongoBaseEntity {

    @Id
    private String id;
    private String botId;
    private String userId;

    public ChatRoom(String botId, String userId) {
        this.botId = botId;
        this.userId = userId;
    }
}
