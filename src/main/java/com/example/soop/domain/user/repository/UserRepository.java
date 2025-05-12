package com.example.soop.domain.user.repository;

import com.example.soop.domain.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByProviderIdAndEmail(String providerId, String email);

    Optional<User> findByProviderIdAndEmail(String providerId, String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long userId);
}
