package com.example.soop.domain.chat.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "soop-chats") // MongoDB 컬렉션 이름
public class Chat {

    @Id
    private String id; // MongoDB는 기본 String ID 사용

    @Field("chat_room_id")
    private Long chatRoomId; // ChatRoom id만 저장

    @Field("sender_id")
    private Long senderId; // User id만 저장

    @Field("content")
    private String content;

    @Builder.Default
    @Field("is_read")
    private Boolean isRead = false;

    @Builder.Default
    @Field("created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성 시간 직접 관리
}
