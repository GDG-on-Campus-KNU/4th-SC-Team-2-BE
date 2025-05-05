package com.example.soop.global.security;

import com.example.soop.domain.user.User;
import com.example.soop.domain.user.repository.UserRepository;
import com.example.soop.global.code.ErrorCode;
import com.example.soop.global.exception.JwtAuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(Long.parseLong(userId))
            .orElseThrow(
                () -> new JwtAuthenticationException(ErrorCode.ACCESS_TOKEN_USER_NOT_FOUND));

        return new CustomUserDetails(user);
    }
}