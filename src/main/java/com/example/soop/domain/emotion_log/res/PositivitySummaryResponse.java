package com.example.soop.domain.emotion_log.res;

import com.example.soop.domain.emotion_report.res.PositivityBlockResponse;

import java.util.List;

public record PositivitySummaryResponse(
        int increaseRate,  // 증가율 (퍼센트)
        List<PositivityBlockResponse> blocks
) {}
