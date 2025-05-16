package com.example.soop.global.config;

import com.example.soop.domain.emotion_log.EmotionGroup;
import com.example.soop.domain.emotion_log.EmotionLog;
import com.example.soop.domain.emotion_log.EmotionLogRepository;
import com.example.soop.domain.user.ExpertProfile;
import com.example.soop.domain.user.User;
import com.example.soop.domain.user.UserType;
import com.example.soop.domain.user.repository.ExpertProfileRepository;
import com.example.soop.domain.user.repository.UserRepository;
import com.example.soop.domain.user.type.Category;
import com.example.soop.domain.user.type.Language;
import com.example.soop.domain.user.type.Style;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.*;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class DataLoaderConfig {

    private final ExpertProfileRepository expertProfileRepository;
    private static final List<String> EMOTION_NAMES = List.of("Joy", "Sadness", "Annoyance", "Calm", "Depression", "Gratitude", "Anxiety", "Happiness", "Lethargy", "Excitement");
    private static final Random random = new Random();

    private static final Map<String, EmotionData> CONTENT_TO_EMOTION = Map.ofEntries(
        Map.entry("I got scolded by my boss at work today and felt angry.", new EmotionData("Anger", EmotionGroup.NEGATIVE)),
        Map.entry("I had dinner with a friend after a long time and felt happy.", new EmotionData("Happiness", EmotionGroup.POSITIVE)),
        Map.entry("I felt stressed because of studying for an exam.", new EmotionData("Stress", EmotionGroup.NEGATIVE)),
        Map.entry("I felt sad watching the rain outside the window.", new EmotionData("Sadness", EmotionGroup.NEGATIVE)),
        Map.entry("I messed up my presentation during a meeting and felt embarrassed.", new EmotionData("Embarrassment", EmotionGroup.NEGATIVE)),
        Map.entry("I felt happy while walking my dog and seeing its cute behavior.", new EmotionData("Happiness", EmotionGroup.POSITIVE)),
        Map.entry("Someone gave up their seat for me on the bus and I felt grateful.", new EmotionData("Gratitude", EmotionGroup.POSITIVE)),
        Map.entry("I felt proud after being praised in class.", new EmotionData("Pride", EmotionGroup.POSITIVE)),
        Map.entry("I was annoyed because I was late to work due to traffic.", new EmotionData("Annoyance", EmotionGroup.NEGATIVE)),
        Map.entry("I felt heavy-hearted after fighting with a friend.", new EmotionData("Sadness", EmotionGroup.NEGATIVE)),
        Map.entry("I felt proud for sticking to my workout routine lately.", new EmotionData("Pride", EmotionGroup.POSITIVE)),
        Map.entry("I felt exhausted and powerless due to work overload.", new EmotionData("Lethargy", EmotionGroup.NEGATIVE)),
        Map.entry("The sunny weather made me feel refreshed.", new EmotionData("Calm", EmotionGroup.POSITIVE)),
        Map.entry("I felt happy spending time with my family at the movies.", new EmotionData("Happiness", EmotionGroup.POSITIVE)),
        Map.entry("It was payday, but I felt bitter because it all went into savings.", new EmotionData("Bitterness", EmotionGroup.NEUTRAL)),
        Map.entry("I felt intimidated watching my classmate's presentation.", new EmotionData("Intimidation", EmotionGroup.NEUTRAL)),
        Map.entry("I felt left out at a group gathering.", new EmotionData("Loneliness", EmotionGroup.NEGATIVE)),
        Map.entry("I enjoyed some peaceful alone time at a café.", new EmotionData("Calm", EmotionGroup.NEUTRAL)),
        Map.entry("I felt happy when a cat approached me and let me pet it.", new EmotionData("Happiness", EmotionGroup.POSITIVE)),
        Map.entry("I felt disappointed in my friend over a small issue.", new EmotionData("Disappointment", EmotionGroup.NEGATIVE)),
        Map.entry("I felt a sense of accomplishment after finishing the project.", new EmotionData("Accomplishment", EmotionGroup.POSITIVE)),
        Map.entry("I was glad to bond with my teammates at the company workshop.", new EmotionData("Connection", EmotionGroup.POSITIVE)),
        Map.entry("I felt regretful after fighting with my partner.", new EmotionData("Regret", EmotionGroup.NEGATIVE)),
        Map.entry("I felt sentimental looking at the stars in the night sky.", new EmotionData("Sentimental", EmotionGroup.NEUTRAL)),
        Map.entry("I felt empty because nothing happened all day.", new EmotionData("Emptiness", EmotionGroup.NEUTRAL)),
        Map.entry("I spilled coffee on my way to work and felt bad all day.", new EmotionData("Annoyance", EmotionGroup.NEGATIVE)),
        Map.entry("I felt touched congratulating a friend at their wedding.", new EmotionData("Joy", EmotionGroup.POSITIVE)),
        Map.entry("I felt good after being praised by my boss.", new EmotionData("Pride", EmotionGroup.POSITIVE)),
        Map.entry("I blamed myself for making a mistake while rushing a task.", new EmotionData("Guilt", EmotionGroup.NEGATIVE)),
        Map.entry("I felt happy seeing a cute dog while walking down the street.", new EmotionData("Happiness", EmotionGroup.POSITIVE)),
        Map.entry("I felt excited planning a trip for the weekend.", new EmotionData("Excitement", EmotionGroup.POSITIVE)),
        Map.entry("I felt nervous after getting an unexpected interview call.", new EmotionData("Nervousness", EmotionGroup.NEUTRAL)),
        Map.entry("I felt frustrated waiting at the bank because of delays.", new EmotionData("Frustration", EmotionGroup.NEGATIVE)),
        Map.entry("I felt happy talking with a senior I admire.", new EmotionData("Happiness", EmotionGroup.POSITIVE)),
        Map.entry("I felt motivated after attending a self-development lecture.", new EmotionData("Motivation", EmotionGroup.POSITIVE)),
        Map.entry("I felt anxious realizing I left my phone at home.", new EmotionData("Anxiety", EmotionGroup.NEUTRAL)),
        Map.entry("I felt amazed becoming friends quickly with a stranger.", new EmotionData("Surprise", EmotionGroup.POSITIVE)),
        Map.entry("I felt happy succeeding in losing weight at the gym.", new EmotionData("Happiness", EmotionGroup.POSITIVE)),
        Map.entry("I felt joyful when my family enjoyed the food I cooked.", new EmotionData("Joy", EmotionGroup.POSITIVE)),
        Map.entry("I felt disappointed when my dinner plans were canceled.", new EmotionData("Disappointment", EmotionGroup.NEGATIVE)),
        Map.entry("I felt grateful when a coworker helped me finish my work.", new EmotionData("Gratitude", EmotionGroup.POSITIVE)),
        Map.entry("I felt moved watching a street performance.", new EmotionData("Moved", EmotionGroup.POSITIVE)),
        Map.entry("I felt nostalgic organizing old photos.", new EmotionData("Nostalgia", EmotionGroup.NEUTRAL)),
        Map.entry("I enjoyed relaxing with a cup of coffee on a rainy day.", new EmotionData("Calm", EmotionGroup.NEUTRAL)),
        Map.entry("I felt discouraged after receiving a job rejection.", new EmotionData("Discouragement", EmotionGroup.NEGATIVE)),
        Map.entry("I felt relieved after getting good results from a medical checkup.", new EmotionData("Relief", EmotionGroup.POSITIVE)),
        Map.entry("I was angry because my package was lost.", new EmotionData("Anger", EmotionGroup.NEGATIVE)),
        Map.entry("I felt happy spending a warm time with my family gathering.", new EmotionData("Happiness", EmotionGroup.POSITIVE)),
        Map.entry("I felt focused reading a book on the subway.", new EmotionData("Focus", EmotionGroup.NEUTRAL)),
        Map.entry("I felt delighted discovering a new hobby.", new EmotionData("Delight", EmotionGroup.POSITIVE))
    );

    @Bean
    @Transactional
    public ApplicationRunner dataLoader(UserRepository userRepository, EmotionLogRepository emotionLogRepository) {
        return args -> {
            User user = new User("google-sub-001", "user1@example.com", "유저1", UserType.USER);
            userRepository.save(user);

            LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
            for (int i = 0; i < 4; i++) {
                LocalDate date = monday.plusDays(i);
                LocalDateTime baseTime = date.atTime(10, 0);

                List<String> contents = new ArrayList<>(CONTENT_TO_EMOTION.keySet());

                for (int j = 0; j < 20; j++) {
                    String content = contents.get(random.nextInt(contents.size()));
                    EmotionData data = CONTENT_TO_EMOTION.get(content);

                    EmotionLog log = new EmotionLog(user, data.emotionName(), data.emotionGroup(), content, baseTime.plusHours(j), 0);
                    emotionLogRepository.save(log);
                }
            }

            System.out.println("✅ 테스트용 다양한 감정 로그 (의미 있는 emotionGroup 포함) 생성 완료");

            // 전문가 User 저장
            List<User> expertUsers = List.of(
                new User("1001", "dr.emily.lee@example.com", "Dr.Emily", UserType.EXPERT),
                new User("1002", "james.kim@example.com", "JamesK", UserType.EXPERT),
                new User("1003", "anna.chan@example.com", "AnnaC", UserType.EXPERT),
                new User("1004", "michael.park@example.com", "MikeP", UserType.EXPERT),
                new User("1005", "suhyun.choi@example.com", "SuhyunC", UserType.EXPERT),
                new User("1006", "kevin.liu@example.com", "KevL", UserType.EXPERT),
                new User("1007", "haruka.tanaka@example.com", "HarukaT", UserType.EXPERT),
                new User("1008", "lisa.jung@example.com", "LisaJ", UserType.EXPERT),
                new User("1009", "daniel.mendoza@example.com", "DanielM", UserType.EXPERT),
                new User("1010", "grace.yoon@example.com", "GraceY", UserType.EXPERT)
            );
            userRepository.saveAll(expertUsers);

            // 전문가 프로필 저장
            List<ExpertProfile> expertProfiles = List.of(
                createExpertProfile(expertUsers.get(0), Category.DOCTOR, 12, List.of(Style.SUPPORTIVE, Style.ANALYTICAL), Language.ENGLISH, "Specialized in cognitive behavioral therapy with over a decade of clinical experience."),
                createExpertProfile(expertUsers.get(1), Category.COUNSELOR, 5, List.of(Style.SUPPORTIVE), Language.KOREAN, "경청과 공감을 바탕으로 하는 마음치유 상담사입니다."),
                createExpertProfile(expertUsers.get(2), Category.PUBLIC_INSTITUTION, 8, List.of(Style.COGNITIVE, Style.SUPPORTIVE), Language.CHINESE, "以人本主义为核心，帮助来访者发现内在力量。"),
                createExpertProfile(expertUsers.get(3), Category.DOCTOR, 10, List.of(Style.DIRECTIVE, Style.ANALYTICAL), Language.ENGLISH, "Licensed psychiatrist focused on trauma recovery and emotional regulation."),
                createExpertProfile(expertUsers.get(4), Category.COUNSELOR, 3, List.of(Style.HOLISTIC, Style.SUPPORTIVE), Language.KOREAN, "내담자의 이야기를 진심으로 들어주는 따뜻한 상담을 지향합니다."),
                createExpertProfile(expertUsers.get(5), Category.PUBLIC_INSTITUTION, 6, List.of(Style.COGNITIVE), Language.ENGLISH, "I help clients reframe negative thoughts and develop healthier habits."),
                createExpertProfile(expertUsers.get(6), Category.COUNSELOR, 4, List.of(Style.HOLISTIC, Style.SUPPORTIVE), Language.JAPANESE, "クライアントの心に寄り添う対話を大切にしています。"),
                createExpertProfile(expertUsers.get(7), Category.DOCTOR, 15, List.of(Style.DIRECTIVE), Language.KOREAN, "정신의학 기반의 분석적 상담으로 실질적인 문제 해결을 돕습니다."),
                createExpertProfile(expertUsers.get(8), Category.PUBLIC_INSTITUTION, 7, List.of(Style.HOLISTIC, Style.COGNITIVE), Language.KOREAN, "Experiencia en terapia familiar y manejo del estrés."),
                createExpertProfile(expertUsers.get(9), Category.COUNSELOR, 2, List.of(Style.MINDFULNESS_BASED), Language.ENGLISH, "I focus on creating a safe, welcoming space for young adults navigating change.")
            );

            expertProfileRepository.saveAll(expertProfiles);
        };
    }
    public record EmotionData(String emotionName, EmotionGroup emotionGroup) {}

    private ExpertProfile createExpertProfile(
        User user,
        Category category,
        int experience,
        List<Style> styles,
        Language language,
        String bio
    ) {
        ExpertProfile profile = new ExpertProfile();
        profile.setUser(user);
        profile.setCategory(category);
        profile.setExperience(experience);
        profile.setStyles(styles);
        profile.setLanguage(language);
        profile.setBio(bio);
        return profile;
    }
}
