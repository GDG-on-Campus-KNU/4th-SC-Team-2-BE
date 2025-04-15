package com.example.soop.domain.emotion_report;

import com.example.soop.domain.emotion_log.EmotionGroup;
import com.example.soop.domain.emotion_log.EmotionLog;
import com.example.soop.domain.emotion_log.EmotionLogRepository;
import com.example.soop.domain.emotion_report.res.EmotionReportResponse;
import com.example.soop.domain.emotion_report.res.EmotionReportResponse.TriggerStat;
import com.example.soop.domain.user.User;
import com.example.soop.domain.user.UserRepository;
import com.example.soop.global.code.ErrorCode;
import com.example.soop.global.exception.UserException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmotionReportService {

    private final EmotionLogRepository emotionLogRepository;
    private final UserRepository userRepository;

    @Description("일별 분석 보고서 조회")
    public EmotionReportResponse getDailyReport(Long userId, LocalDate date) {
        return getReport(userId, date, date.plusDays(1));
    }

    @Description("주별 분석 보고서 조회")
    public EmotionReportResponse getWeeklyReport(Long userId, LocalDate dateInWeek) {
        LocalDate start = dateInWeek.with(DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(7);
        return getReport(userId, start, end);
    }

    @Description("월별 분석 보고서 조회")
    public EmotionReportResponse getMonthlyReport(Long userId, LocalDate dateInMonth) {
        LocalDate start = dateInMonth.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);
        return getReport(userId, start, end);
    }

    @Description("기간별 보고서 생성 조회")
    public EmotionReportResponse getReport(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = findUser(userId);
        List<EmotionLog> logs = emotionLogRepository.findAllByUserAndRecordedAtGreaterThanEqualAndRecordedAtLessThan(
            user, startDate.atStartOfDay(), endDate.atStartOfDay());

        int total = logs.size(); // 감정 로그 총 개수
        // 긍정/중립/부정 별로 몇 개의 로그가 있는지 카운트
        Map<EmotionGroup, Long> groupCounts = logs.stream() 
            .collect(Collectors.groupingBy(EmotionLog::getEmotionGroup, Collectors.counting()));
        // 감정의 이름별로 등장 횟수 카운트
        Map<String, Long> emotionCounts = logs.stream() 
            .collect(Collectors.groupingBy(EmotionLog::getEmotionName, Collectors.counting()));
        // 가장 많이 등장한 감정
        String mostFrequentEmotion = emotionCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("None"); 
        // 가장 많이 등장한 감정의 비율
        int mostFreqPercent = (int) (100 * emotionCounts.getOrDefault(mostFrequentEmotion, 0L) / (double) Math.max(total, 1));
        // 가장 적게 등장한 감정
        String leastFrequentEmotion = emotionCounts.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("None");
        // 가장 적게 등장한 감정의 비율
        int leastFreqPercent = (int) (100 * emotionCounts.getOrDefault(leastFrequentEmotion, 0L) / (double) Math.max(total, 1));
        // 긍정/중립/부정 감정에 대해 트리거 추출 (상위 3개)
        List<TriggerStat> positiveTriggerStats = extractTriggers(logs, EmotionGroup.POSITIVE);
        List<TriggerStat> neutralTriggerStats = extractTriggers(logs, EmotionGroup.NEUTRAL);
        List<TriggerStat> negativeTriggerStats = extractTriggers(logs, EmotionGroup.NEGATIVE);
        // 최종 응답 생성
        return new EmotionReportResponse(
            total,
            Map.of(
                EmotionGroup.POSITIVE, (int) (100 * groupCounts.getOrDefault(EmotionGroup.POSITIVE, 0L) / (double) Math.max(total, 1)),
                EmotionGroup.NEUTRAL,  (int) (100 * groupCounts.getOrDefault(EmotionGroup.NEUTRAL, 0L) / (double) Math.max(total, 1)),
                EmotionGroup.NEGATIVE, (int) (100 * groupCounts.getOrDefault(EmotionGroup.NEGATIVE, 0L) / (double) Math.max(total, 1))
            ),
            mostFrequentEmotion,
            mostFreqPercent,
            leastFrequentEmotion,
            leastFreqPercent,
            positiveTriggerStats,
            negativeTriggerStats
        );
    }

    private List<TriggerStat> extractTriggers(List<EmotionLog> logs, EmotionGroup group) {
        Map<String, Long> triggers = logs.stream()
            .filter(log -> log.getEmotionGroup() == group)
            .map(EmotionLog::getContent)
            .flatMap(content -> Arrays.stream(content.split("[ ,.]")))
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return triggers.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .map(e -> new TriggerStat(e.getKey(), e.getValue().intValue()))
            .toList();
    }

    public User findUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        return user;
    }



}
