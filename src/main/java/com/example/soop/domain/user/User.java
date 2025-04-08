package com.example.soop.domain.user;

import com.example.soop.global.entity.JpaBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "users")
@Entity
@Data
public class User extends JpaBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 구글 sub 값 (고유 식별자)
    @Column(nullable = false, unique = true)
    private String providerId;

    // 이메일 (중복 방지용)
    @Column(nullable = false, unique = true)
    private String email;

    // 닉네임 (서비스 전용, 사용자 입력)
    @Column(nullable = false, unique = true)
    private String nickname;

    public User() {

    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public User(String providerId, String email, String nickname) {
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
    }
}
