package com.example.soop.domain.chat.entity;

import com.example.soop.domain.chat.RoomType;
import com.example.soop.global.entity.JpaBaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chat_rooms")
public class ChatRoom extends JpaBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private RoomType roomType;

    @Column
    private LocalDateTime messageUpdatedAt; // 가장 최근 메시지 도착 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Membership> memberships = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "chat_room_info_id", nullable = false)
    private ChatRoomInfo chatRoomInfo;

    // 가장 최근 메시지 도착 시간 업데이트
    public void updateMessageUpdatedAt() {
        this.messageUpdatedAt = LocalDateTime.now();
    }
}
