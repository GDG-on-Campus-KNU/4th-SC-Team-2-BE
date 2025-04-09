package com.example.soop.domain.chat;

import com.example.soop.domain.chat.req.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    @MessageMapping("/chat/send")
    public void handleMessage(@Payload ChatMessageRequest messageRequest) {
        System.out.println("messageRequest = " + messageRequest.toString());
        // 1. 메시지 저장
        ChatMessage message = new ChatMessage();
        message.setRoomId(messageRequest.roomId());
        message.setSender(messageRequest.sender());
        message.setMessage(messageRequest.message());
        chatMessageRepository.save(message);

        // 2. 메시지 전송 (구독자에게)
        messagingTemplate.convertAndSend("/topic/chat/" + messageRequest.roomId(), message);
    }
}
