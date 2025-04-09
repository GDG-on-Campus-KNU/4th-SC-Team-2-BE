package com.example.soop.domain.emotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {

}
