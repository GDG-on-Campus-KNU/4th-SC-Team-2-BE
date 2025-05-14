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
import java.util.stream.Collectors;
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
    private final SearchService searchService;
    private final WebClient.Builder webClientBuilder;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Value("${gemini.api-key}")
    private String apiKey;

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
            String aiResponse = callGemini(userMessage, conversationHistory, chatRoomInfo);

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
     * 실제 Gemini AI API 호출 부분
     */
    public String callGemini(String userMessage, List<Map<String, String>> conversationHistory, ChatRoomInfo chatRoomInfo) {
        log.info("📡 Gemini API 호출 시작...");
        // 신뢰 자료 수집 (본문 포함)
        List<Map<String, String>> searchResults = searchService.searchAndCrawlTop2WithBody(userMessage);
        log.info("searchResults: {}", searchResults);
        List<Map<String, String>> messages = prepareMessagesGeminiAI(userMessage, conversationHistory, chatRoomInfo, searchResults);

        String prompt = messages.stream()
            .map(m -> m.get("role") + ": " + m.get("content"))
            .collect(Collectors.joining("\n\n"));

        // Gemini API 요청
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
            )
        );
        log.info("📨 요청 프롬프트: {}", prompt);

        String answer = webClientBuilder.build()
            .post()
            .uri(apiUrl + "?key=" + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> {
                try {
                    Map<?, ?> candidate = (Map<?, ?>) ((List<?>) response.get("candidates")).get(0);
                    Map<?, ?> content = (Map<?, ?>) candidate.get("content");
                    List<?> parts = (List<?>) content.get("parts");
                    Map<?, ?> part = (Map<?, ?>) parts.get(0);
                    return part.get("text").toString();
                } catch (Exception e) {
                    return "오늘 많이 지쳤죠. 너무 잘 버티고 있어요. 천천히 나아가도 괜찮아요.";
                }
            })
            .block();
        log.info("🧠 Gemini 응답: {}", answer);
        return answer;

    }

    /**
     * 실제 OpenAI API 호출 부분
     */
    public String callOpenAi(String userMessage, List<Map<String, String>> conversationHistory, ChatRoomInfo chatRoomInfo) {
        List<Map<String, String>> messages = prepareMessagesOpenAI(userMessage, conversationHistory, chatRoomInfo);

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
    private List<Map<String, String>> prepareMessagesGeminiAI(
        String userMessage,
        List<Map<String, String>> conversationHistory,
        ChatRoomInfo chatRoomInfo,
        List<Map<String, String>> searchResults) {

        List<Map<String, String>> messages = new ArrayList<>();

        String referenceTexts = searchResults.stream()
            .map(r -> r.getOrDefault("text", ""))
            .filter(t -> !t.isBlank())
            .collect(Collectors.joining("\n\n---\n\n"));

        String systemPrompt = String.format(
            "당신은 '%s'라는 AI 챗봇입니다. 설명: %s. 공감 레벨: %s. 톤: %s.\n\n" +
                "아래는 신뢰할 수 있는 참고 자료입니다. 반드시 이 자료를 바탕으로 답변을 구성하고, 출처는 ~ 에 따르면 이라고 맨앞에 추가하세요.:\n\n%s",
            chatRoomInfo.getName(),
            chatRoomInfo.getDescription(),
            chatRoomInfo.getEmpathyLevel(),
            chatRoomInfo.getTone(),
            referenceTexts
        );

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            messages.addAll(conversationHistory);
        }

        Map<String, String> userMessageMap = new HashMap<>();
        userMessageMap.put("role", "user");
        userMessageMap.put("content", userMessage);
        messages.add(userMessageMap);

        return messages;
    }

    /**
     * 대화 이력을 포함한 메시지 준비
     */
    private List<Map<String, String>> prepareMessagesOpenAI(String userMessage,
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
