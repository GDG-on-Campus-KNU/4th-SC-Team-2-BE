package com.example.soop.domain.emotion_report;


import com.example.soop.domain.emotion_report.EmotionReportService.AiTriggersAndFeedbackResult;
import com.example.soop.domain.emotion_report.res.EmotionReportResponse;
import com.example.soop.global.format.ApiResponse;
import com.example.soop.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.soop.domain.emotion_report.res.PositivityBlockResponse;
import java.util.List;
import com.example.soop.domain.emotion_log.res.PositivitySummaryResponse;
import com.example.soop.domain.emotion_report.EmotionReportService.TriggerResult;

@Tag(name = "Emotion Report", description = "감정 분석 관련 API")
@RestController
@RequestMapping("/api/v1/emotion-report")
@RequiredArgsConstructor
public class EmotionReportController {

    private final EmotionReportService emotionReportService;

    @Operation(summary = "오늘 감정 분석 조회")
    @GetMapping("/daily")
    public ApiResponse<EmotionReportResponse> getDailyReport(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        EmotionReportResponse dailyReport = emotionReportService.getReport(userDetails.getId(),
            today,
            today.plusDays(1));
        return ApiResponse.createSuccessWithData(dailyReport, "오늘 감정 분석 조회에 성공했습니다.");
    }

    @Operation(summary = "이번 주 감정 분석 조회")
    @GetMapping("/weekly")
    public ApiResponse<EmotionReportResponse> getWeeklyReport(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY).plusDays(1);
        EmotionReportResponse weeklyReport = emotionReportService.getReport(userDetails.getId(),
            startOfWeek, endOfWeek);
        return ApiResponse.createSuccessWithData(weeklyReport, "이번 주 감정 분석 조회에 성공했습니다.");
    }

    @Operation(summary = "이번 달 감정 분석 조회")
    @GetMapping("/monthly")
    public ApiResponse<EmotionReportResponse> getMonthlyReport(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).plusDays(1);
        EmotionReportResponse monthlyReport = emotionReportService.getReport(userDetails.getId(),
            startOfMonth, endOfMonth);
        return ApiResponse.createSuccessWithData(monthlyReport, "이번 달 감정 분석 조회에 성공했습니다.");
    }

    @Operation(summary = "기간별 감정 분석 조회")
    @GetMapping
    public ApiResponse<EmotionReportResponse> getReportByPeriod(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam("startDate") LocalDate startDate,
        @RequestParam("endDate") LocalDate endDate
    ) {
        // endDate 포함을 위해 하루 뒤까지 조회
        EmotionReportResponse periodReport = emotionReportService.getReport(userDetails.getId(),
            startDate, endDate.plusDays(1));
        return ApiResponse.createSuccessWithData(periodReport, "기간별 감정 통계 조회에 성공했습니다.");
    }

    @Operation(summary = "기간별 AI 트리거 및 전략 조회")
    @GetMapping("/ai-feedback")
    public ApiResponse<AiTriggersAndFeedbackResult> getAiFeedback(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AiTriggersAndFeedbackResult aiFeedbackResult = emotionReportService.generateTriggersAndStrategies(
            userDetails.getId(), startDate, endDate);
        return ApiResponse.createSuccessWithData(aiFeedbackResult, "기간별 AI 트리거 및 전략 조회에 성공했습니다.");
    }

    @Operation(summary = "최근 7일간 긍정 감정 블록 + 증가율 요약")
    @GetMapping("/positivity-summary")
    public ApiResponse<PositivitySummaryResponse> getPositivitySummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PositivitySummaryResponse summary =
                emotionReportService.getWeeklyPositivitySummary(userDetails.getId());
        return ApiResponse.createSuccessWithData(summary, "긍정 감정 요약 조회에 성공했습니다.");
    }

    @Operation(summary = "최근 가장 많이 등장한 긍정 트리거 조회")
    @GetMapping("/top-positive-trigger")
    public ApiResponse<TriggerResult> getTopPositiveTrigger(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6); // 최근 7일
        TriggerResult result = emotionReportService.getMostPositiveTrigger(userDetails.getId(), start, today.plusDays(1));
        return ApiResponse.createSuccessWithData(result, "최근 가장 자주 등장한 긍정 트리거 조회에 성공했습니다.");
    }

}
