package com.example.soop.domain.emotion_report;


import com.example.soop.domain.emotion_report.res.EmotionReportResponse;
import com.example.soop.domain.user.User;
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

@Tag(name = "Emotion Report", description = "감정 분석 관련 API")
@RestController
@RequestMapping("/api/v1/emotion-report")
@RequiredArgsConstructor
public class EmotionReportController {

    private final EmotionReportService emotionReportService;

    @Operation(summary = "오늘 감정 분석 조회")
    @GetMapping("/daily")
    public EmotionReportResponse getDailyReport(@AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        return emotionReportService.getReport(userDetails.getId(), today, today.plusDays(1));
    }

    @Operation(summary = "이번 주 감정 분석 조회")
    @GetMapping("/weekly")
    public EmotionReportResponse getWeeklyReport(@AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY).plusDays(1);
        return emotionReportService.getReport(userDetails.getId(), startOfWeek, endOfWeek);
    }

    @Operation(summary = "이번 달 감정 분석 조회")
    @GetMapping("/monthly")
    public EmotionReportResponse getMonthlyReport(@AuthenticationPrincipal CustomUserDetails userDetails) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).plusDays(1);
        return emotionReportService.getReport(userDetails.getId(), startOfMonth, endOfMonth);
    }

    @Operation(summary = "기간 별 감정 분석 조회")
    @GetMapping
    public EmotionReportResponse getReportByPeriod(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam("startDate") LocalDate startDate,
        @RequestParam("endDate") LocalDate endDate
    ) {
        // endDate 포함을 위해 하루 뒤까지 조회
        return emotionReportService.getReport(userDetails.getId(), startDate, endDate.plusDays(1));
    }
}
