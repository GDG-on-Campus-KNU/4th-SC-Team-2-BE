package com.example.soop.domain.emotion_log.res;

import java.time.DayOfWeek;
import java.util.List;

public record DayEmotionLogResponse(
    DayOfWeek dayOfWeek,
    List<EmotionLogResponse> emotions
) {

}
