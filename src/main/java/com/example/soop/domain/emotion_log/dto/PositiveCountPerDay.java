package com.example.soop.domain.emotion_log.dto;

import java.time.LocalDate;

public interface PositiveCountPerDay {
    LocalDate getDay();
    Long getCount();
}
