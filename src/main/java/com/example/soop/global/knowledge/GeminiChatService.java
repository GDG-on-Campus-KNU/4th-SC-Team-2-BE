package com.example.soop.global.knowledge;

import com.example.soop.domain.chat.ChatService;
import com.example.soop.domain.chat.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeminiChatService {

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Value("${gemini.api-key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;
    private final ChatService chatService;
    private final SearchService searchService;

    public String ask(String question, Long chatRoomId) {
        // 사용자 메시지 저장
        Chat userChat = Chat.builder()
                .chatRoomId(chatRoomId)
                .senderId(1L)
                .content(question)
                .isRead(true)
                .build();
        chatService.saveChat(userChat);

        // 최근 10개 대화 이력 불러오기
        List<Chat> history = chatService.getRecentChats(chatRoomId, 10);

        // 🔍 신뢰 자료 도메인 추출
        List<Map<String, String>> searchResults = searchService.searchAndCrawlTop2WithBody(question);
        String referenceDomains = searchResults.stream()
                .map(r -> searchService.getDomain(r.getOrDefault("link", "")))
                .distinct()
                .limit(2)
                .collect(Collectors.joining(", "));

        // 🧠 프롬프트 구성
        StringBuilder prompt = new StringBuilder("""
            당신은 따뜻하고 전문적인 심리 상담사입니다.
            아래 사용자의 대화와 신뢰할 수 있는 정보를 바탕으로
            공감되며 실질적인 도움이 되는 말을 100자 이내로 해주세요.
            참고한 정보가 있다면, 답변 마지막에 다음과 같이 괄호로 덧붙여 주세요:

            (참고 자료: 도메인1, 도메인2)

            [대화 이력]
        """);

        for (Chat chat : history) {
            String role = (chat.getSenderId() == 1L) ? "user" : "assistant";
            prompt.append(String.format("\n[%s] %s", role, chat.getContent()));
        }

        prompt.append("\n[user] ").append(question);
        prompt.append("\n[assistant]");

        // Gemini API 요청
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt.toString())))
                )
        );

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

        // Gemini 응답 저장
        Chat botChat = Chat.builder()
                .chatRoomId(chatRoomId)
                .senderId(0L)
                .content(answer)
                .isRead(false)
                .build();
        chatService.saveChat(botChat);

        return answer;
    }
}
