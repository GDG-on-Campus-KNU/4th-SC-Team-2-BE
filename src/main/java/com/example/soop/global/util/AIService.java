package com.example.soop.global.util;

import com.example.soop.domain.chat.ChatService;
import com.example.soop.domain.chat.dto.res.ChatContentResponse;
import com.example.soop.domain.chat.entity.Chat;
import com.example.soop.domain.chat.entity.ChatRoom;
import com.example.soop.domain.chat.entity.ChatRoomInfo;
import com.example.soop.global.redis.RedisPublisher;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatService chatService;
    private final RedisPublisher redisPublisher;
    private final ChatLanguageModel chatLanguageModel; // ✅ LangChain4j 기반 모델 주입

    @Async
    public void generateAndPublishResponseAsync(Long chatRoomId, String userMessage,
                                                List<Map<String, String>> conversationHistory) {
        try {
            ChatRoom chatRoom = chatService.getChatRoomById(chatRoomId);
            ChatRoomInfo chatRoomInfo = chatRoom.getChatRoomInfo();

            String aiResponse = generateResponse(userMessage, conversationHistory, chatRoomInfo);

            Chat botChat = Chat.builder()
                    .chatRoomId(chatRoomId)
                    .senderId(0L)
                    .content(aiResponse)
                    .isRead(false)
                    .build();

            Chat savedBotChat = chatService.saveChat(botChat);

            ChatContentResponse botResponse = new ChatContentResponse(
                    savedBotChat.getId(),
                    chatRoomId,
                    0L,
                    savedBotChat.getContent(),
                    savedBotChat.getCreatedAt()
            );

            redisPublisher.publish(botResponse);
        } catch (Exception e) {
            log.error("❌ Gemini 응답 생성 중 오류: {}", e.getMessage(), e);
            handleAiError(chatRoomId, e);
        }
    }

    public String generateResponse(String userMessage, List<Map<String, String>> history, ChatRoomInfo info)
    {
        StringBuilder prompt = new StringBuilder();

        // ✅ 시스템 프롬프트
        prompt.append(String.format("""
            당신은 '%s'라는 AI 챗봇입니다.
            설명: %s
            공감 레벨: %s
            말투: %s
            """, info.getName(), info.getDescription(), info.getEmpathyLevel(), info.getTone()));

        // ✅ 대화 이력 추가
        if (history != null) {
            for (Map<String, String> entry : history) {
                prompt.append("\n[").append(entry.get("role")).append("] ")
                        .append(entry.get("content"));
            }
        }

        prompt.append("\n[user] ").append(userMessage);

        log.info("📝 LangChain4j에 전달할 프롬프트:\n{}", prompt);

        try {
            String result = chatLanguageModel.chat(prompt.toString());  // ✅ 수정
            log.info("💬 Gemini 응답: {}", result);
            return result;
        } catch (Exception e) {
            log.error("❌ LangChain4j ChatModel 오류: {}", e.getMessage(), e);
            return "AI 응답 생성 중 오류가 발생했습니다.";
        }

    }

    private void handleAiError(Long chatRoomId, Exception e) {
        String errorMessage = "AI 응답 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";

        Chat errorChat = Chat.builder()
                .chatRoomId(chatRoomId)
                .senderId(0L)
                .content(errorMessage)
                .isRead(false)
                .build();

        Chat savedErrorChat = chatService.saveChat(errorChat);

        ChatContentResponse errorResponse = new ChatContentResponse(
                savedErrorChat.getId(),
                chatRoomId,
                0L,
                savedErrorChat.getContent(),
                savedErrorChat.getCreatedAt()
        );

        redisPublisher.publish(errorResponse);
    }
}
