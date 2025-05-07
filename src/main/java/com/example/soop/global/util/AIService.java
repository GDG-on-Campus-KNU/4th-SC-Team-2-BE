package com.example.soop.global.util;

import com.example.soop.domain.chat.ChatService;
import com.example.soop.domain.chat.dto.res.ChatContentResponse;
import com.example.soop.domain.chat.entity.Chat;
import com.example.soop.domain.chat.entity.ChatRoom;
import com.example.soop.domain.chat.entity.ChatRoomInfo;
import com.example.soop.global.redis.RedisPublisher;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;


@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatService chatService;
    private final RedisPublisher redisPublisher;
    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4o"; // Latest model as of April 2025
    private static final int MAX_RETRIES = 2;
    private static final int TIMEOUT_SECONDS = 60;


    /**
     * 비동기로 GPT 응답을 생성하고 저장 및 Publish 까지 처리
     */
    @Async
    public void generateAndPublishResponseAsync(Long chatRoomId, String userMessage,
        List<Map<String, String>> conversationHistory) {
        try {
            ChatRoom chatRoom = chatService.getChatRoomById(chatRoomId);
            ChatRoomInfo chatRoomInfo = chatRoom.getChatRoomInfo();

            // ✅ 1. OpenAI 호출
            String aiResponse = callOpenAi(userMessage, conversationHistory, chatRoomInfo);

            // ✅ 2. GPT 답변 저장
            Chat botChat = Chat.builder()
                .chatRoomId(chatRoomId)
                .senderId(0L) // 시스템 ID
                .content(aiResponse)
                .isRead(false)
                .build();
            Chat savedBotChat = chatService.saveChat(botChat);

            // ✅ 3. Redis Publish
            ChatContentResponse botResponse = new ChatContentResponse(
                savedBotChat.getId(),
                chatRoomId,
                0L,
                savedBotChat.getContent(),
                savedBotChat.getCreatedAt()
            );
            redisPublisher.publish(botResponse);
        } catch (Exception e) {
            log.error("GPT 응답 생성 비동기 처리 중 오류: {}", e.getMessage(), e);
            handleGptError(chatRoomId, e);
        }
    }

    /**
     * 실제 OpenAI API 호출 부분
     */
    public String callOpenAi(String userMessage, List<Map<String, String>> conversationHistory, ChatRoomInfo chatRoomInfo) {
        List<Map<String, String>> messages = prepareMessages(userMessage, conversationHistory, chatRoomInfo);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", DEFAULT_MODEL);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        return webClient.post()
            .uri(OPENAI_URL)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofSeconds(2))
                .filter(throwable -> throwable instanceof WebClientResponseException
                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
            .map(responseBody -> {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get(
                    "choices");
                Map<String, Object> choice = choices.get(0);
                Map<String, String> message = (Map<String, String>) choice.get("message");
                return message.get("content").trim();
            })
            .onErrorResume(TimeoutException.class, ex -> {
                log.error("OpenAI API 요청 타임아웃: {}", ex.getMessage());
                return Mono.just("죄송합니다. 응답 생성에 시간이 너무 오래 걸립니다. 잠시 후 다시 시도해주세요.");
            })
            .onErrorResume(WebClientResponseException.class, ex -> {
                log.error("OpenAI API 호출 오류: {}, 상태 코드: {}", ex.getMessage(), ex.getStatusCode());
                return Mono.just("OpenAI API 연결 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            })
            .onErrorResume(Exception.class, ex -> {
                log.error("예상치 못한 오류: {}", ex.getMessage(), ex);
                return Mono.just("서비스 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            })
            .block();
    }


    /**
     * 대화 이력을 포함한 메시지 준비
     */
    private List<Map<String, String>> prepareMessages(String userMessage,
        List<Map<String, String>> conversationHistory, ChatRoomInfo chatRoomInfo) {
        List<Map<String, String>> messages = new ArrayList<>();

        // ✅ system 메시지: chatRoomInfo 기반 생성
        String systemPrompt = String.format(
            "당신은 '%s'라는 AI 챗봇입니다. 설명: %s. 공감 레벨: %s. 톤: %s. 이 특성에 맞게 답변하세요.",
            chatRoomInfo.getName(),
            chatRoomInfo.getDescription(),
            chatRoomInfo.getEmpathyLevel(),
            chatRoomInfo.getTone()
        );

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        // ✅ 기존 대화 이력
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            messages.addAll(conversationHistory);
        }

        // ✅ 사용자 메시지
        Map<String, String> userMessageMap = new HashMap<>();
        userMessageMap.put("role", "user");
        userMessageMap.put("content", userMessage);
        messages.add(userMessageMap);

        return messages;
    }

    /**
     * GPT 오류 처리
     */
    private void handleGptError(Long chatRoomId, Exception e) {
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

    /**
     * WebClient 빈 생성 - GptConfig 클래스에 추가하는 것이 좋습니다
     */
    /*
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }
    */
}
