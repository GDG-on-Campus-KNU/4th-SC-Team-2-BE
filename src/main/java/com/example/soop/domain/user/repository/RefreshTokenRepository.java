package com.example.soop.domain.user.repository;

import com.example.soop.domain.user.RefreshToken;
import com.example.soop.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String refreshToken);

    Optional<RefreshToken> findByUser(User user);

    Boolean existsByToken(String refreshToken);
}
