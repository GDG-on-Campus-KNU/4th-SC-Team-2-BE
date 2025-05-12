package com.example.soop.domain.knowledge.service;

import com.example.soop.domain.knowledge.entity.SavedReference;
import com.example.soop.domain.knowledge.repository.SavedReferenceRepository;
import com.example.soop.global.util.VectorUtils;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityService {

    private final SavedReferenceRepository savedReferenceRepository;
    private final VertexAiEmbeddingModel vertexAiEmbeddingModel;

    public List<SavedReference> findSimilarReferences(String question, int limit) {
        try {
            // 1. 질문을 임베딩
            float[] vector = vertexAiEmbeddingModel.embed(question).content().vector();
            String pgVector = VectorUtils.toPgVectorString(vector);

            // 2. 유사한 문서 top-k 검색
            List<SavedReference> similarDocs = savedReferenceRepository.findTopKBySimilarity(pgVector, limit);

            // 3. 로그 출력 (선택사항)
            for (SavedReference ref : similarDocs) {
                log.info("🔍 유사한 문서: {}", ref.getSourceTitle());
            }

            return similarDocs;

        } catch (Exception e) {
            log.error("❌ 유사 문서 검색 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
