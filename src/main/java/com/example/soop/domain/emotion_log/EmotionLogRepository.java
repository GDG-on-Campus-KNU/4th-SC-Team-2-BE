package com.example.soop.domain.emotion_log;

import com.example.soop.domain.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {

    List<EmotionLog> findAllByUserAndRecordedAtBetweenOrderByRecordedAtAsc(User user, LocalDateTime start, LocalDateTime end);

    List<EmotionLog> findAllByUserAndRecordedAtBetween(User user, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<EmotionLog> findAllByUserAndRecordedAtGreaterThanEqualAndRecordedAtLessThan(User user, LocalDateTime start, LocalDateTime end);

    List<EmotionLog> findByRecordedAtBetween(LocalDateTime start, LocalDateTime end);

    List<EmotionLog> findByUserAndRecordedAtBetween(User user, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
