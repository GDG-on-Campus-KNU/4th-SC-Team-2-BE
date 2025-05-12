package com.example.soop.domain.emotion_report.res;

public record PositivityBlockResponse(
        String day,  // 예: "Mon", "Tue"
        int count,   // 긍정 감정 횟수
        int blocks   // 표시할 블록 수 (1~3)
) {}
