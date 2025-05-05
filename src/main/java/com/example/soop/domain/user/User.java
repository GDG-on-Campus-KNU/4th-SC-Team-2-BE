package com.example.soop.domain.user;
import com.example.soop.global.entity.JpaBaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "users")
@Entity
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType = UserType.USER;  // 기본값 일반 사용자

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ExpertProfile expertProfile;  // 전문가 정보 (없으면 일반 사용자)

    public User() {

    }

    public User(String providerId, String email, String nickname, UserType userType) {
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
        this.userType = userType;
    }
}
