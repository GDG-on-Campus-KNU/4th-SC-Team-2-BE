package com.example.soop.domain.user;

import com.example.soop.domain.user.req.LoginRequest;
import com.example.soop.domain.user.req.SignupRequest;
import com.example.soop.global.code.ErrorCode;
import com.example.soop.global.exception.UserException;
import com.example.soop.global.jwt.JwtProvider;
import com.example.soop.global.jwt.TokenResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Tag(name = "User", description = "유저 및 권한 관련 API")
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByProviderIdAndEmail(request.providerId(), request.email())) {
            throw new UserException(ErrorCode.USER_DUPLICATED);
        }
        userRepository.save(new User(request.providerId(), request.email(), request.nickname()));
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByProviderIdAndEmail(request.providerId(), request.email())
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken();

        refreshTokenRepository.findByUser(user)
            .ifPresentOrElse(
                token -> token.update(refreshToken), // 존재 O -> 업데이트
                () -> refreshTokenRepository.save(new RefreshToken(user, refreshToken))
                // 존재 X -> 생성
            );

        return new TokenResponse(accessToken, refreshToken);
    }

}
