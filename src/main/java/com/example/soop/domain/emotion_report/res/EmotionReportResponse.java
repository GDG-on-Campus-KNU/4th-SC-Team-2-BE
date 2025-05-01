package com.example.soop.domain.emotion_report.res;

import com.example.soop.domain.emotion_log.EmotionGroup;
import java.util.Map;

public record EmotionReportResponse(
    int totalLogs,
    Map<EmotionGroup, Integer> emotionGroupCounts,
    String mostFrequentEmotion,
    int mostFrequentPercentage,
    String leastFrequentEmotion,
    int leastFrequentPercentage
) {

}

