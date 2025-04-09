package com.example.soop.domain.emotion;

import com.example.soop.domain.user.User;
import com.example.soop.global.entity.JpaBaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class EmotionLog extends JpaBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    private String emotionName; // 감정 이름 (ex. 기쁨, 슬픔, 우울, 무력, 커스텀 감정명 등..)

    @Enumerated(EnumType.STRING)
    private EmotionGroup emotionGroup; // 감정 그룹 (POSITIVE / NEGATIVE / NEUTRAL)

    private String content; // 감정에 대한 상세 설명

    private LocalDateTime recordedAt = LocalDateTime.now();

    public EmotionLog(User user, String emotionName, EmotionGroup emotionGroup, String content,  LocalDateTime recordedAt) {
        this.user = user;
        this.emotionName = emotionName;
        this.emotionGroup = emotionGroup;
        this.content = content;
        this.recordedAt = recordedAt;
    }

    public void update(String emotionName, EmotionGroup emotionGroup, String content,
        LocalDateTime recordedAt) {
        this.emotionName = emotionName;
        this.emotionGroup = emotionGroup;
        this.content = content;
        this.recordedAt = recordedAt;
    }
}
