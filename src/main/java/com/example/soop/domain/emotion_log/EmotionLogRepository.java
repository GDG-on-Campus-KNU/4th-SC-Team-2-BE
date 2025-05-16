package com.example.soop.domain.emotion_log;

import com.example.soop.domain.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.soop.domain.emotion_log.dto.PositiveCountPerDay;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {
    @Query("""
    SELECT CAST(e.recordedAt AS date) AS day, COUNT(e) AS count
    FROM EmotionLog e
    WHERE e.user = :user
      AND e.emotionGroup = 'POSITIVE'
      AND e.recordedAt >= :start
      AND e.recordedAt < :end
    GROUP BY CAST(e.recordedAt AS date)
    ORDER BY CAST(e.recordedAt AS date)
""")
    List<PositiveCountPerDay> countPositiveByDay(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    List<EmotionLog> findAllByUserAndRecordedAtBetweenOrderByRecordedAtAsc(User user,
                                                                           LocalDateTime start, LocalDateTime end);

    List<EmotionLog> findAllByUserAndRecordedAtBetween(User user, LocalDateTime startDateTime,
                                                       LocalDateTime endDateTime);

    List<EmotionLog> findAllByUserAndRecordedAtGreaterThanEqualAndRecordedAtLessThan(User user,
                                                                                     LocalDateTime start, LocalDateTime end);

    List<EmotionLog> findByRecordedAtBetween(LocalDateTime start, LocalDateTime end);

    List<EmotionLog> findByUserAndRecordedAtBetween(User user, LocalDateTime startDateTime,
                                                    LocalDateTime endDateTime);
}