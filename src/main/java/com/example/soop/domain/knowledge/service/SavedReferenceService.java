package com.example.soop.domain.knowledge.service;

import com.example.soop.domain.knowledge.entity.SavedReference;
import com.example.soop.domain.knowledge.repository.SavedReferenceRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedReferenceService {

    private final SavedReferenceRepository repository;
    private final EmbeddingModel embeddingModel;

    // 🔹 1. 저장
    public void save(SavedReference ref) {
        repository.save(ref);
    }

    // 🔹 2. 전체 불러오기
    public List<SavedReference> findAll() {
        return repository.findAll();
    }

    // 🔹 3. 유사한 문서 검색 (임베딩 + 코사인 유사도)
    public List<SavedReference> findTopKSimilar(String query, int topK, double threshold) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        return repository.findAll().stream()
                .map(ref -> {
                    double similarity = cosineSimilarity(queryEmbedding.vector(), ref.getEmbeddingVector());
                    ref.setSimilarity(similarity);  // 유사도 필드 추가 필요 (임시 저장용)
                    return ref;
                })
                .filter(ref -> ref.getSimilarity() >= threshold)
                .sorted(Comparator.comparingDouble(SavedReference::getSimilarity).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    // 🔹 코사인 유사도 계산
    private double cosineSimilarity(float[] v1, float[] v2) {
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        return denominator == 0 ? 0.0 : dot / denominator;
    }
}
