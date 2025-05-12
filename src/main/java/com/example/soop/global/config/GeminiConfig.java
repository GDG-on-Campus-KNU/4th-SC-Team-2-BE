package com.example.soop.global.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return VertexAiGeminiChatModel.builder()
                .project("soop-456213")
                .location("asia-northeast3")
                .modelName("gemini-1.5-flash-002")
                .maxOutputTokens(1000)
                .build();
    }
}
