package com.example.soop.domain.emotion_log;

import com.example.soop.domain.emotion_log.req.CreateEmotionLogRequest;
import com.example.soop.domain.emotion_log.req.UpdateEmotionLogRequest;
import com.example.soop.domain.emotion_log.res.DailyTopEmotionResponse;
import com.example.soop.domain.emotion_log.res.DayEmotionLogResponse;
import com.example.soop.domain.emotion_log.res.EmotionLogResponse;
import com.example.soop.domain.user.User;
import com.example.soop.domain.user.UserRepository;
import com.example.soop.global.code.ErrorCode;
import com.example.soop.global.exception.EmotionLogException;
import com.example.soop.global.exception.UserException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmotionLogService {


    private final EmotionLogRepository emotionLogRepository;
    private final UserRepository userRepository;

    @Transactional
    @Description("감정 기록 생성")
    public EmotionLog createEmotionLog(Long userId, CreateEmotionLogRequest request) {
        User user = findUser(userId);
        EmotionLog emotionLog = new EmotionLog(user, request.emotionName(), request.emotionGroup(),
            request.content(), request.recordedAt());
        return emotionLogRepository.save(emotionLog);
    }

    @Transactional
    @Description("감정 기록 삭제")
    public void deleteEmotionLog(Long emotionLogId) {
        EmotionLog emotionLog = emotionLogRepository.findById(emotionLogId)
            .orElseThrow(() -> new EmotionLogException(ErrorCode.EMOTION_NOT_FOUND));
        emotionLogRepository.delete(emotionLog);
    }

    @Transactional
    @Description("감정 기록 수정")
    public void updateEmotionLog(Long emotionLogId,
        UpdateEmotionLogRequest updateEmotionLogRequest) {
        EmotionLog emotionLog = emotionLogRepository.findById(emotionLogId)
            .orElseThrow(() -> new EmotionLogException(ErrorCode.EMOTION_NOT_FOUND));
        emotionLog.update(updateEmotionLogRequest.emotionName(),
            updateEmotionLogRequest.emotionGroup(), updateEmotionLogRequest.content(),
            updateEmotionLogRequest.recordedAt());
    }

    @Description("감정 기록 개별 조회")
    public EmotionLogResponse getEmotionLog(Long emotionLogId) {
        EmotionLog emotionLog = emotionLogRepository.findById(emotionLogId)
            .orElseThrow(() -> new EmotionLogException(ErrorCode.EMOTION_NOT_FOUND));
        return EmotionLogResponse.fromEntity(emotionLog);
    }

    @Description("감정 기록 일별 조회")
    public DayEmotionLogResponse getDailyEmotionLogs(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        User user = findUser(userId);
        List<EmotionLogResponse> emotionLogs = emotionLogRepository
            .findAllByUserAndRecordedAtBetweenOrderByRecordedAtAsc(user, start, end)
            .stream()
            .map(EmotionLogResponse::fromEntity)
            .toList();
        return new DayEmotionLogResponse(dayOfWeek, emotionLogs);
    }

    @Description("감정 기록 주별 조회 - 특정 날짜(anyDayInWeek)가 속한 1주일 간의 감정 로그를, 요일별로 리스트로 묶어서 반환")
    public List<DayEmotionLogResponse> getWeeklyEmotionLogs(Long userId, LocalDate anyDayInWeek) {
        LocalDate start = anyDayInWeek.with(DayOfWeek.MONDAY);
        LocalDate end = anyDayInWeek.with(DayOfWeek.SUNDAY);

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        User user = findUser(userId);
        List<EmotionLog> logs = emotionLogRepository.findAllByUserAndRecordedAtBetween(user,
            startDateTime, endDateTime);

        // 요일별로 그룹화
        Map<DayOfWeek, List<EmotionLogResponse>> grouped = logs.stream()
            .collect(Collectors.groupingBy(
                l -> l.getRecordedAt().getDayOfWeek(),
                Collectors.mapping(EmotionLogResponse::fromEntity, Collectors.toList())
            ));

        // 월~일 순서 보장 + 없는 요일은 빈 리스트
        return Arrays.stream(DayOfWeek.values())
            .map(day -> new DayEmotionLogResponse(day, grouped.getOrDefault(day, List.of())))
            .toList();
    }

    public User findUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        return user;
    }

    @Description("감정 기록 월별 조회 - 날짜별로 가장 많이 등록된 감정 이름 조회")
    public List<DailyTopEmotionResponse> getMonthlyTopEmotionPerDay(Long userId,
        YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        User user = findUser(userId);

        List<EmotionLog> logs = emotionLogRepository.findAllByUserAndRecordedAtBetween(
            user,
            startDate.atStartOfDay(),
            endDate.atTime(LocalTime.MAX)
        );

        // 날짜별로 그룹 → 감정 이름으로 또 그룹 → 개수 세기 → 최빈 감정 찾기
        return logs.stream()
            .collect(Collectors.groupingBy(log -> log.getRecordedAt().toLocalDate()))
            .entrySet().stream()
            .map(entry -> {
                LocalDate date = entry.getKey();
                List<EmotionLog> logsOnDate = entry.getValue();

                // 감정별로 개수 세기
                String mostFrequentEmotion = logsOnDate.stream()
                    .collect(
                        Collectors.groupingBy(EmotionLog::getEmotionName, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue()) // 최빈 감정
                    .map(Map.Entry::getKey)
                    .orElse("NONE");

                return new DailyTopEmotionResponse(date, mostFrequentEmotion);
            })
            .sorted(Comparator.comparing(DailyTopEmotionResponse::date))
            .toList();
    }
}
