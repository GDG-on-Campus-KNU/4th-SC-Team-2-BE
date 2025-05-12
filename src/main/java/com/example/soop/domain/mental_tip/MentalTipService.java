package com.example.soop.domain.mental_tip;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.soop.domain.mental_tip.res.MentalTipResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MentalTipService {

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Value("${gemini.api-key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;

    // ✅ 하나의 팁 생성
    public String generateSingleTip() {
        String prompt = """
            Please give me a short, friendly, daily mental health tip.
            Keep it simple, positive, and under 30 words.
        """;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        return webClientBuilder.build()
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
                        return "Take a deep breath and smile today!";
                    }
                })
                .block();
    }

    // ✅ 세 개의 팁 생성
    public MentalTipResponse generateThreeTips() {
        List<String> tips = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tips.add(generateSingleTip());
        }
        return new MentalTipResponse(tips);
    }
}
