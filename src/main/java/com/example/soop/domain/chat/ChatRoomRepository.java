package com.example.soop.domain.chat;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    Optional<ChatRoom> findByBotIdAndUserId(String first, String second);
}
