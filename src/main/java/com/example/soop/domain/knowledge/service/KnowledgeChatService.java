package com.example.soop.domain.knowledge.service;

import com.example.soop.domain.knowledge.entity.SavedReference;
import com.example.soop.domain.knowledge.repository.SavedReferenceRepository;
import com.example.soop.global.util.VectorUtils;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeChatService {

    private final ChatLanguageModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final SavedReferenceRepository savedReferenceRepository;

    public String askWithRetrieval(String question) {
        try {
            // 1️⃣ 질문 임베딩
            Embedding embedding = embeddingModel.embed(question).content();
            String pgVector = VectorUtils.toPgVectorString(embedding.vector());

            // 2️⃣ 유사 문서 검색 (pgvector)
            List<SavedReference> references = savedReferenceRepository.findTopKBySimilarity(pgVector, 3);

            StringBuilder context = new StringBuilder();
            for (SavedReference ref : references) {
                context.append(ref.getSourceText()).append("\n");
            }

            // 3️⃣ Prompt 구성
            String prompt = String.format("""
                당신은 공감 능력이 뛰어난 심리 상담사입니다.

                질문: %s

                아래 참고 문서를 바탕으로 답변해 주세요:
                %s
                """, question, context.toString());

            // 4️⃣ Gemini 호출
            return chatModel.chat(prompt);

        } catch (Exception e) {
            return "❌ 오류 발생: " + e.getMessage();
        }
    }
}
