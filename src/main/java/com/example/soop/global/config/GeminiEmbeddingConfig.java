package com.example.soop.global.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiEmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return VertexAiEmbeddingModel.builder()
                .project("soop-456213")
                .location("asia-northeast3")
                .publisher("google")
                .modelName("text-embedding-005")
                .endpoint("asia-northeast3-aiplatform.googleapis.com:443")
                .maxRetries(3)
                .build();
    }
}
