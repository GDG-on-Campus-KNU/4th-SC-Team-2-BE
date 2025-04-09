package com.example.soop.domain.emotion.res;

import java.time.DayOfWeek;
import java.util.List;

public record DayEmotionLogResponse(
    DayOfWeek dayOfWeek,
    List<EmotionLogResponse> emotions
) {}
