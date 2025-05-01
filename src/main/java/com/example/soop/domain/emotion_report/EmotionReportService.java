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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class EmotionReportService {

    private final EmotionLogRepository emotionLogRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();  // HTTP 호출용
    @Value("${gemini.api-url}")
    private String geminiApiUrlBase;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    String GEMINI_API_URL = geminiApiUrlBase + "?key=" + geminiApiKey;

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
        int mostFreqPercent = (int) (100 * emotionCounts.getOrDefault(mostFrequentEmotion, 0L)
            / (double) Math.max(total, 1));
        // 가장 적게 등장한 감정
        String leastFrequentEmotion = emotionCounts.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("None");
        // 가장 적게 등장한 감정의 비율
        int leastFreqPercent = (int) (100 * emotionCounts.getOrDefault(leastFrequentEmotion, 0L)
            / (double) Math.max(total, 1));
        // 긍정/중립/부정 감정에 대해 트리거 추출 (상위 3개)
        List<TriggerStat> positiveTriggerStats = extractTriggers(logs, EmotionGroup.POSITIVE);
        List<TriggerStat> neutralTriggerStats = extractTriggers(logs, EmotionGroup.NEUTRAL);
        List<TriggerStat> negativeTriggerStats = extractTriggers(logs, EmotionGroup.NEGATIVE);
        // 최종 응답 생성
        return new EmotionReportResponse(
            total,
            Map.of(
                EmotionGroup.POSITIVE,
                (int) (100 * groupCounts.getOrDefault(EmotionGroup.POSITIVE, 0L)
                    / (double) Math.max(total, 1)),
                EmotionGroup.NEUTRAL,
                (int) (100 * groupCounts.getOrDefault(EmotionGroup.NEUTRAL, 0L) / (double) Math.max(
                    total, 1)),
                EmotionGroup.NEGATIVE,
                (int) (100 * groupCounts.getOrDefault(EmotionGroup.NEGATIVE, 0L)
                    / (double) Math.max(total, 1))
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

    public Map<EmotionGroup, List<TriggerResult>> printTop3TriggersByPeriod(Long userId,
        LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        User user = userRepository.findById(userId).get();
        List<EmotionLog> logs = emotionLogRepository.findByUserAndRecordedAtBetween(user,
            startDateTime, endDateTime);

        if (logs.isEmpty()) {
            System.out.println("⚠️ 해당 기간에 EmotionLog 데이터가 없습니다.");
            return null;
        }

        List<String> contents = logs.stream().map(EmotionLog::getContent).toList();

        StringBuilder prompt = new StringBuilder("""
            For each description, return its corresponding trigger in the following format:

            <number>. Trigger: <trigger_name> (trigger_name should be a short phrase, no more than 30 characters)

            Descriptions:
        """);

        for (int i = 0; i < contents.size(); i++) {
            prompt.append(String.format("%d. \"%s\"\n", i + 1, contents.get(i)));
        }

        // Gemini API 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt.toString())
                ))
            )
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, Map.class);

        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Gemini 응답이 비어있습니다.");
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini 응답에 candidates가 없습니다.");
        }

        Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");

        String responseText = (String) parts.get(0).get("text");
        System.out.println("📝 Gemini 응답:\n" + responseText);

        // emotionGroup별 trigger count (POSITIVE, NEGATIVE만)
        Map<EmotionGroup, Map<String, Integer>> triggerCounts = new HashMap<>();
        triggerCounts.put(EmotionGroup.POSITIVE, new HashMap<>());
        triggerCounts.put(EmotionGroup.NEGATIVE, new HashMap<>());

        for (String line : responseText.split("\n")) {
            if (line.contains("Trigger:")) {
                try {
                    String[] partsLine = line.split("Trigger:");
                    int index = Integer.parseInt(partsLine[0].replace(".", "").trim()) - 1;
                    String trigger = partsLine[1].trim();

                    EmotionGroup group = logs.get(index).getEmotionGroup();

                    if (group == EmotionGroup.POSITIVE || group == EmotionGroup.NEGATIVE) {
                        triggerCounts.get(group).merge(trigger, 1, Integer::sum);
                    }
                    // else 무시
                } catch (Exception e) {
                    System.out.println("⚠️ 응답 파싱 오류: " + line);
                }
            }
        }

        // 각 그룹별 상위 3개 추출 (POSITIVE, NEGATIVE만)
        Map<EmotionGroup, List<TriggerResult>> result = new HashMap<>();

        for (EmotionGroup group : List.of(EmotionGroup.POSITIVE, EmotionGroup.NEGATIVE)) {
            List<TriggerResult> top3 = triggerCounts.get(group).entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(3)
                .map(e -> new TriggerResult(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
            result.put(group, top3);
        }

        result.forEach((group, list) -> {
            System.out.println("상위 3개 트리거 [" + group + "]:");
            list.forEach(tr -> System.out.printf("%s %d회\n", tr.trigger(), tr.count()));
        });

        return result;
    }
    public record TriggerResult(String trigger, int count) {

    }


}
