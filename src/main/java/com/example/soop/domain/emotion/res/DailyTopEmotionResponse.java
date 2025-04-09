package com.example.soop.domain.emotion.res;

import java.time.LocalDate;

public record DailyTopEmotionResponse(
    LocalDate date,
    String topEmotionName
) {
}
