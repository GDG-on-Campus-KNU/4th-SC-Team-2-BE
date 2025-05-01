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
import java.util.ArrayList;
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

    public AiFeedbackResult generateTriggersAndStrategies(Long userId, LocalDate startDate,
        LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        User user = userRepository.findById(userId).orElseThrow();
        List<EmotionLog> logs = emotionLogRepository.findByUserAndRecordedAtBetween(user,
            startDateTime, endDateTime);

        if (logs.isEmpty()) {
            System.out.println("⚠️ 해당 기간에 EmotionLog 데이터가 없습니다.");
            return new AiFeedbackResult(
                List.of(),  // positiveTriggers
                List.of(),  // negativeTriggers
                List.of(),  // positiveStrategies
                List.of()   // negativeStrategies
            );
        }


        List<String> contents = logs.stream().map(EmotionLog::getContent).toList();

        StringBuilder prompt = new StringBuilder("""
            For each description below:
            1. Extract a trigger as the situation or action (NOT the emotion felt).
            - The trigger should describe **the event or action that caused the emotion**.
            - Keep the trigger between 15 and 30 characters.
            - Include key subjects or actions (e.g., "Fight with friend", "Boss praise", "Package lost").
            - Avoid general emotional labels like "Anger", "Happiness", "Disappointment".
                        
            Then:
            - Group similar triggers if they refer to the same situation or action (but DO NOT overgeneralize).
            - Classify each trigger as POSITIVE or NEGATIVE based on the emotion it caused.
            - Count how many times each trigger appeared per group.
            - Return ONLY the TOP 3 triggers per group based on frequency.

            Finally:
            For each group (POSITIVE and NEGATIVE):
            - Suggest exactly 3 actionable strategies to strengthen positive triggers or mitigate negative triggers.

            Return result in this format (NO MORE THAN 3 TRIGGERS per group):

            Positive Triggers:
            1. <trigger> → <count>
            2. <trigger> → <count>
            3. <trigger> → <count>

            Positive Strategies:
            - <strategy 1>
            - <strategy 2>
            - <strategy 3>

            Negative Triggers:
            1. <trigger> → <count>
            2. <trigger> → <count>
            3. <trigger> → <count>

            Negative Strategies:
            - <strategy 1>
            - <strategy 2>
            - <strategy 3>

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
        String geminiApiUrl = geminiApiUrlBase + "?key=" + geminiApiKey;
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(geminiApiUrl, requestEntity,
            Map.class);

        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Gemini 응답 비어있음");
        }
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get(
            "candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini 응답에 candidates 없음");
        }
        Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
        String responseText = (String) parts.get(0).get("text");

        System.out.println("📝 Gemini 응답:\n" + responseText);

        Map<EmotionGroup, List<TriggerResult>> triggerMap = new HashMap<>();
        Map<EmotionGroup, List<String>> strategyMap = new HashMap<>();
        triggerMap.put(EmotionGroup.POSITIVE, new ArrayList<>());
        triggerMap.put(EmotionGroup.NEGATIVE, new ArrayList<>());
        strategyMap.put(EmotionGroup.POSITIVE, new ArrayList<>());
        strategyMap.put(EmotionGroup.NEGATIVE, new ArrayList<>());

        EmotionGroup currentGroup = null;
        boolean readingTriggers = false;
        boolean readingStrategies = false;

        for (String line : responseText.split("\n")) {
            line = line.trim();
            if (line.startsWith("Positive Triggers:")) {
                currentGroup = EmotionGroup.POSITIVE;
                readingTriggers = true;
                readingStrategies = false;
            } else if (line.startsWith("Positive Strategies:")) {
                currentGroup = EmotionGroup.POSITIVE;
                readingTriggers = false;
                readingStrategies = true;
            } else if (line.startsWith("Negative Triggers:")) {
                currentGroup = EmotionGroup.NEGATIVE;
                readingTriggers = true;
                readingStrategies = false;
            } else if (line.startsWith("Negative Strategies:")) {
                currentGroup = EmotionGroup.NEGATIVE;
                readingTriggers = false;
                readingStrategies = true;
            } else if (readingTriggers && line.matches("^\\d+\\.\\s+.*→\\s*\\d+")) {
                String[] partsLine = line.split("→");
                String trigger = partsLine[0].replaceAll("^\\d+\\.\\s+", "").trim();
                int count = Integer.parseInt(partsLine[1].trim());
                triggerMap.get(currentGroup).add(new TriggerResult(trigger, count));
            } else if (readingStrategies && line.startsWith("-")) {
                String strategy = line.substring(1).trim();
                strategyMap.get(currentGroup).add(strategy);
            }
        }

        return new AiFeedbackResult(
            triggerMap.getOrDefault(EmotionGroup.POSITIVE, List.of()),
            triggerMap.getOrDefault(EmotionGroup.NEGATIVE, List.of()),
            strategyMap.getOrDefault(EmotionGroup.POSITIVE, List.of()),
            strategyMap.getOrDefault(EmotionGroup.NEGATIVE, List.of())
        );

    }

    public record TriggerResult(String trigger, int count) {

    }

    public record AiFeedbackResult(
        List<TriggerResult> positiveTriggers,
        List<TriggerResult> negativeTriggers,
        List<String> positiveStrategies,
        List<String> negativeStrategies
    ) {}

}
