package com.example.soop.domain.chat.entity;

import com.example.soop.domain.chat.type.EmpathyLevel;
import com.example.soop.domain.chat.type.ToneLevel;
import com.example.soop.domain.user.User;
import jakarta.persistence.CascadeType;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // = botId

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "chatRoomInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoom> chatRooms = new ArrayList<>();

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private EmpathyLevel empathyLevel;

    @Enumerated(EnumType.STRING)
    private ToneLevel tone;

    private int image;
}
