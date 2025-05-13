package com.example.soop.domain.chat;

import com.example.soop.domain.chat.dto.req.ChatRequest;
import com.example.soop.domain.chat.dto.res.ChatContentResponse;
import com.example.soop.domain.chat.entity.Chat;
import com.example.soop.global.redis.RedisPublisher;
import com.example.soop.global.util.AIService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatService chatService;
    private final AIService AIService;
    private final RedisPublisher redisPublisher; // Redis 발행기

    @MessageMapping("/chat")
    public void handleChat(Message<?> message, ChatRequest chatRequest) {
        log.info("✅ handleChat 진입, chatRoomId: {}, content: {}", chatRequest.chatRoomId(), chatRequest.content());
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        Long chatRoomId = chatRequest.chatRoomId();

        log.info("CHAT message, chatRequest: {}", chatRequest);

        // 1. 사용자 메시지 저장 + 발행 (바로!)
        handleUserMessage(chatRequest, chatRoomId, userId);

        // 2. RoomType 확인
        RoomType roomType = chatService.getRoomType(chatRoomId);

        // 3. RoomType 별 처리
        if (roomType == RoomType.USER_TO_BOT) {
            log.info("✅ USER_TO_BOT 확인 완료, Gemini 호출 준비");
            // 대화 이력 가져오기 (최근 N개 메시지)
            List<Map<String, String>> conversationHistory = getConversationHistory(chatRoomId);

            // Gemini 응답은 "비동기"로 처리 - 개선된 버전 호출
            AIService.generateAndPublishResponseAsync(chatRoomId, chatRequest.content(), conversationHistory);

            // 스트리밍 응답을 사용하고 싶은 경우 아래 코드 사용
            // gptService.callOpenAiWithStreaming(chatRoomId, chatRequest.content(), conversationHistory);
        } else if (roomType == RoomType.USER_TO_EXPERT) {
            // 관리자 채팅은 추가 처리 없음
        } else {
            throw new IllegalStateException("지원하지 않는 RoomType 입니다: " + roomType);
        }

        // 4. 최근 채팅방 업데이트
        chatService.updateChatRoomMessageUpdatedTime(chatRoomId);
    }

    private void handleUserMessage(ChatRequest chatRequest, Long chatRoomId, Long userId) {
        Chat chat = Chat.builder()
            .chatRoomId(chatRoomId)
            .senderId(userId)
            .content(chatRequest.content())
            .isRead(false)
            .build();

        Chat savedChat = chatService.saveChat(chat);

        ChatContentResponse response = new ChatContentResponse(
            savedChat.getId(),
            chatRoomId,
            userId,
            savedChat.getContent(),
            savedChat.getCreatedAt()
        );

        redisPublisher.publish(response); // 저장과 동시에 Publish
    }

    /**
     * 대화 이력을 가져오는 메소드
     * 최근 N개의 메시지를 가져와 OpenAI API 형식에 맞게 변환
     */
    private List<Map<String, String>> getConversationHistory(Long chatRoomId) {
        // 최근 대화 이력 조회 (예: 최근 10개)
        List<Chat> recentChats = chatService.getRecentChats(chatRoomId, 10);
        List<Map<String, String>> conversationHistory = new ArrayList<>();

        for (Chat chat : recentChats) {
            Map<String, String> message = new HashMap<>();

            // 사용자 메시지와 AI 메시지 구분
            if (chat.getSenderId() > 0) { // 사용자 메시지
                message.put("role", "user");
            } else { // AI 메시지
                message.put("role", "assistant");
            }

            message.put("content", chat.getContent());
            conversationHistory.add(message);
        }

        return conversationHistory;
    }

}
