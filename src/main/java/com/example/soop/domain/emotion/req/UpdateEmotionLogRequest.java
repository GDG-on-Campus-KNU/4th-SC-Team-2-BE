package com.example.soop.domain.emotion.req;

import com.example.soop.domain.emotion.EmotionGroup;
import java.time.LocalDateTime;

public record UpdateEmotionLogRequest(
    String emotionName,
    EmotionGroup emotionGroup,
    String content,
    LocalDateTime recordedAt
) {

}
