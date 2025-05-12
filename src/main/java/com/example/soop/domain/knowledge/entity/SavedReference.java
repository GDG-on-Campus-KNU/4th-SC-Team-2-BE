package com.example.soop.domain.knowledge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import com.example.soop.global.util.FloatArrayToPgVectorConverter;

@Entity
@Table(name = "saved_reference")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourceTitle;
    private String sourceLink;

    @Column(length = 5000)
    private String sourceText;

    @Column(name = "embedding_vector", columnDefinition = "vector(1536)")
    @Convert(converter = FloatArrayToPgVectorConverter.class)
    private float[] embeddingVector;

    // 유사도 임시 저장용 (정렬 등에 사용)
    @Transient
    private double similarity;

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

}
