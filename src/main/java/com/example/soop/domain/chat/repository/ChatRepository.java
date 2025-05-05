package com.example.soop.domain.chat.repository;

import com.example.soop.domain.chat.entity.Chat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<Chat, String> {

    List<Chat> findAllByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    List<Chat> findAllByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

    List<Chat> findAllByChatRoomIdAndSenderId(Long chatRoomId, Long senderId);

    Optional<Chat> findTopByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

    Optional<Chat> findByIdAndChatRoomId(String chatId, Long chatRoomId);

    List<Chat> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, PageRequest of);
}
