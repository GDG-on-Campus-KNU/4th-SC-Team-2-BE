package com.example.soop.domain.emotion_log;

import com.example.soop.domain.emotion_log.req.CreateEmotionLogRequest;
import com.example.soop.domain.emotion_log.req.UpdateEmotionLogRequest;
import com.example.soop.domain.emotion_log.res.DailyTopEmotionResponse;
import com.example.soop.domain.emotion_log.res.DayEmotionLogResponse;
import com.example.soop.domain.emotion_log.res.EmotionLogResponse;
import com.example.soop.global.format.ApiResponse;
import com.example.soop.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Emotion Log", description = "감정 기록 관련 API")
@RestController
@RequestMapping("/api/v1/emotion-logs")
@RequiredArgsConstructor
public class EmotionLogController {

    private final EmotionLogService emotionLogService;

    @Operation(summary = "감정 기록 생성")
    @PostMapping
    public ApiResponse<String> createEmotionLog(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        CreateEmotionLogRequest createEmotionLogRequest) {
        emotionLogService.createEmotionLog(userDetails.getId(), createEmotionLogRequest);
        return ApiResponse.createSuccess("감정이 기록되었습니다.");
    }

    @Operation(summary = "감정 기록 조회")
    @GetMapping("{id}")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "EMOTION401", description = "감정 기록이 존재하지 않습니다."),
    })
    public ApiResponse<EmotionLogResponse> getEmotionLog(
        @PathVariable Long id
    ) {
        EmotionLogResponse emotionLogResponse = emotionLogService.getEmotionLog(id);
        return ApiResponse.createSuccessWithData(emotionLogResponse, "감정 기록 조회에 성공했습니다.");
    }

    @Operation(summary = "감정 기록 수정")
    @PutMapping("{id}")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "EMOTION401", description = "감정 기록이 존재하지 않습니다."),
    })
    public ApiResponse<EmotionLogResponse> updateEmotionLog(
        @PathVariable Long id,
        UpdateEmotionLogRequest updateEmotionLogRequest
    ) {
        emotionLogService.updateEmotionLog(id, updateEmotionLogRequest);
        return ApiResponse.createSuccess("감정 기록이 수정되었습니다.");
    }

    @Operation(summary = "감정 기록 삭제")
    @DeleteMapping("{id}")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "EMOTION401", description = "감정 기록이 존재하지 않습니다."),
    })
    public ApiResponse<String> deleteEmotionLog(
        @PathVariable Long id
    ) {
        emotionLogService.deleteEmotionLog(id);
        return ApiResponse.createSuccess("감정 기록이 삭제되었습니다.");
    }

    @Operation(summary = "일별 감정 기록 조회")
    @GetMapping("/daily")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "EMOTION401", description = "감정 기록이 존재하지 않습니다."),
    })
    public ApiResponse<DayEmotionLogResponse> getDailyEmotionLogs(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam LocalDate localDate
    ) {
        DayEmotionLogResponse dailyEmotionLogs = emotionLogService.getDailyEmotionLogs(
            userDetails.getId(), localDate);
        return ApiResponse.createSuccessWithData(dailyEmotionLogs, "일별 감정 기록이 조회되었습니다.");
    }

    @Operation(summary = "주별 감정 기록 조회")
    @GetMapping("/weekly")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "EMOTION401", description = "감정 기록이 존재하지 않습니다."),
    })
    public ApiResponse<List<DayEmotionLogResponse>> getWeeklyEmotionLogs(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam LocalDate localDate
    ) {
        List<DayEmotionLogResponse> weeklyEmotionLogs = emotionLogService.getWeeklyEmotionLogs(
            userDetails.getId(), localDate);
        return ApiResponse.createSuccessWithData(weeklyEmotionLogs, "주별 감정 기록이 조회되었습니다.");
    }

    @Operation(summary = "월별 감정 기록 조회")
    @GetMapping("/monthly")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "EMOTION401", description = "감정 기록이 존재하지 않습니다."),
    })
    public ApiResponse<List<DailyTopEmotionResponse>> getMonthlyEmotionLogs(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam YearMonth yearMonth
    ) {
        List<DailyTopEmotionResponse> monthlyTopEmotionPerDay = emotionLogService.getMonthlyTopEmotionPerDay(
            userDetails.getId(), yearMonth);
        return ApiResponse.createSuccessWithData(monthlyTopEmotionPerDay, "월별 감정 기록이 조회되었습니다.");
    }


}
