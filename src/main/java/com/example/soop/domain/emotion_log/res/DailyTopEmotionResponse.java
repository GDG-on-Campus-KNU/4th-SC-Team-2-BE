package com.example.soop.domain.emotion_log.res;

import java.time.LocalDate;

public record DailyTopEmotionResponse(
    LocalDate date,
    String topEmotionName
) {

}
