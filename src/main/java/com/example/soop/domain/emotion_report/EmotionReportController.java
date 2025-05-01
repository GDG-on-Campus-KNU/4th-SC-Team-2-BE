package com.example.soop.domain.emotion_report;


import com.example.soop.domain.emotion_log.EmotionGroup;
import com.example.soop.domain.emotion_report.EmotionReportService.AiFeedbackResult;
import com.example.soop.domain.emotion_report.EmotionReportService.TriggerResult;
import com.example.soop.domain.emotion_report.res.EmotionReportResponse;
import com.example.soop.global.format.ApiResponse;
import com.example.soop.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Emotion Report", description = "감정 분석 관련 API")
@RestController
@RequestMapping("/api/v1/emotion-report")
@RequiredArgsConstructor
public class EmotionReportController {

    private final EmotionReportService emotionReportService;

    @Operation(summary = "오늘 감정 분석 조회")
    @GetMapping("/daily")
    public ApiResponse<EmotionReportResponse> getDailyReport(@AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        EmotionReportResponse dailyReport = emotionReportService.getReport(userDetails.getId(), today,
            today.plusDays(1));
        return ApiResponse.createSuccessWithData(dailyReport, "오늘 감정 분석 조회에 성공했습니다.");
    }

    @Operation(summary = "이번 주 감정 분석 조회")
    @GetMapping("/weekly")
    public ApiResponse<EmotionReportResponse> getWeeklyReport(@AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY).plusDays(1);
        EmotionReportResponse weeklyReport = emotionReportService.getReport(userDetails.getId(),
            startOfWeek, endOfWeek);
        return ApiResponse.createSuccessWithData(weeklyReport, "이번 주 감정 분석 조회에 성공했습니다.");
    }

    @Operation(summary = "이번 달 감정 분석 조회")
    @GetMapping("/monthly")
    public ApiResponse<EmotionReportResponse> getMonthlyReport(@AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).plusDays(1);
        EmotionReportResponse monthlyReport = emotionReportService.getReport(userDetails.getId(),
            startOfMonth, endOfMonth);
        return ApiResponse.createSuccessWithData(monthlyReport, "이번 달 감정 분석 조회에 성공했습니다.");
    }

    @Operation(summary = "기간 별 감정 분석 조회")
    @GetMapping
    public ApiResponse<EmotionReportResponse> getReportByPeriod(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam("startDate") LocalDate startDate,
        @RequestParam("endDate") LocalDate endDate
    ) {
        // endDate 포함을 위해 하루 뒤까지 조회
        EmotionReportResponse periodReport = emotionReportService.getReport(userDetails.getId(),
            startDate, endDate.plusDays(1));
        return ApiResponse.createSuccessWithData(periodReport, "기간 별 감정 분석 조회에 성공했습니다.");
    }

    @GetMapping("/ai-feedback")
    public AiFeedbackResult getAiFeedback(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AiFeedbackResult aiFeedbackResult = emotionReportService.generateTriggersAndStrategies(
            userDetails.getId(), startDate, endDate);
        return aiFeedbackResult;
    }

}
