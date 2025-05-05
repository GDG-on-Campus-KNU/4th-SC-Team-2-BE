package com.example.soop.global.redis;

import com.example.soop.domain.chat.dto.res.ChatContentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()); // LocalDateTime 지원 등록!


    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            ChatContentResponse chatContentResponse = objectMapper.readValue(body, ChatContentResponse.class);

            // (STOMP) 실제로 각 서버의 SimpMessagingTemplate로 뿌려줌
            messagingTemplate.convertAndSend("/sub/chatroom/" + chatContentResponse.chatRoomId(), chatContentResponse);
        } catch (Exception e) {
            log.error("Redis Subscriber 에러", e);
        }
    }
}