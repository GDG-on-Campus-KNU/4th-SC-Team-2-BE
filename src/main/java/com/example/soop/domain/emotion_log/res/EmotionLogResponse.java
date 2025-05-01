package com.example.soop.domain.emotion_log.res;

import com.example.soop.domain.emotion_log.EmotionGroup;
import com.example.soop.domain.emotion_log.EmotionLog;
import java.time.LocalDateTime;

public record EmotionLogResponse(
    String emotionName,
    EmotionGroup emotionGroup,
    String content,
    LocalDateTime recordedAt
) {

    public static EmotionLogResponse fromEntity(EmotionLog emotionLog) {
        return new EmotionLogResponse(
            emotionLog.getEmotionName(),
            emotionLog.getEmotionGroup(),
            emotionLog.getContent(),
            emotionLog.getRecordedAt()
        );
    }
}
