package com.example.soop.domain.emotion_log.req;

import com.example.soop.domain.emotion_log.EmotionGroup;
import java.time.LocalDateTime;

public record CreateEmotionLogRequest(
    String emotionName,
    EmotionGroup emotionGroup,
    String content,
    LocalDateTime recordedAt,
    int image
) {

}
