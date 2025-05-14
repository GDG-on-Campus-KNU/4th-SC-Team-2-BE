package com.example.soop.domain.mental_tip;

import com.example.soop.domain.mental_tip.res.MentalTipResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mental-tip")
public class MentalTipController {

    private final MentalTipService mentalTipService;

    @Operation(
        summary = "오늘의 멘탈 팁 3개 조회",
        description = "하루에 세 개, 긍정적인 마음을 위한 멘탈 팁을 제공합니다."
    )
    @GetMapping
    public MentalTipResponse getTodayMentalTips() {
        return mentalTipService.generateThreeTips();
    }
}