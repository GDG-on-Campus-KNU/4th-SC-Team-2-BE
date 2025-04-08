package com.example.soop.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByProviderIdAndEmail(String providerId, String email);

    Optional<User> findByProviderIdAndEmail(String providerId, String email);
}
