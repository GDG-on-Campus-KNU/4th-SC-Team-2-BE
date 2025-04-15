package com.example.soop.global.config;

import com.example.soop.domain.emotion_log.EmotionGroup;
import com.example.soop.domain.emotion_log.EmotionLog;
import com.example.soop.domain.emotion_log.EmotionLogRepository;
import com.example.soop.domain.user.User;
import com.example.soop.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.*;
import java.util.List;
import java.util.Random;

@Configuration
public class DataLoaderConfig {

    private static final List<String> EMOTION_NAMES = List.of("기쁨", "슬픔", "짜증", "평온", "우울", "감사", "불안", "행복", "무기력", "흥분");
    private static final List<EmotionGroup> EMOTION_GROUPS = List.of(EmotionGroup.POSITIVE, EmotionGroup.NEUTRAL, EmotionGroup.NEGATIVE);
    private static final Random random = new Random();

    @Bean
    @Transactional
    public ApplicationRunner dataLoader(UserRepository userRepository, EmotionLogRepository emotionLogRepository) {
        return args -> {

            // 유저 2명 생성
            User user1 = new User("google-sub-001", "user1@example.com", "유저1");
            User user2 = new User("google-sub-002", "user2@example.com", "유저2");
            userRepository.saveAll(List.of(user1, user2));

            List<User> users = List.of(user1, user2);

            // 이번 주 월요일부터 목요일까지
            LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
            for (int i = 0; i < 4; i++) { // 월~목 = 4일
                LocalDate date = monday.plusDays(i);
                LocalDateTime baseTime = date.atTime(10, 0);

                for (User user : users) {
                    for (int j = 0; j < 20; j++) {
                        String emotion = EMOTION_NAMES.get(random.nextInt(EMOTION_NAMES.size()));
                        EmotionGroup group = EMOTION_GROUPS.get(random.nextInt(EMOTION_GROUPS.size()));
                        String content = "테스트 감정 내용 " + (j + 1);

                        EmotionLog log = new EmotionLog(user, emotion, group, content, baseTime.plusHours(j));
                        emotionLogRepository.save(log);
                    }
                }
            }

            System.out.println("✅ 테스트용 감정 로그 생성 완료");
        };
    }
}
