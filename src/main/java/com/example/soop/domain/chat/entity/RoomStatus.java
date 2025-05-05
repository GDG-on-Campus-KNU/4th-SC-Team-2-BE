package com.example.soop.domain.chat.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomStatus {
    ENABLED,    // 활성화(default)
    DISABLED,   // 비활성화
    DELETED;    // 삭제
}
