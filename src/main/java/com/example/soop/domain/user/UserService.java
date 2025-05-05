package com.example.soop.domain.user;

import com.example.soop.domain.user.repository.ExpertProfileRepository;
import com.example.soop.domain.user.repository.RefreshTokenRepository;
import com.example.soop.domain.user.repository.UserRepository;
import com.example.soop.domain.user.req.ExpertSignupRequest;
import com.example.soop.domain.user.req.LoginRequest;
import com.example.soop.domain.user.req.GeneralSignupRequest;
import com.example.soop.domain.user.res.UserResponse;
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
    private final ExpertProfileRepository expertProfileRepository;

    @Transactional
    public void signupGeneral(GeneralSignupRequest request) {
        if (userRepository.existsByProviderIdAndEmail(request.providerId(), request.email())) {
            throw new UserException(ErrorCode.USER_DUPLICATED);
        }
        User user = new User(request.providerId(), request.email(), request.nickname(), UserType.USER);
        userRepository.save(user);
    }
    @Transactional
    public void signupExpert(ExpertSignupRequest request) {
        if (userRepository.existsByProviderIdAndEmail(request.providerId(), request.email())) {
            throw new UserException(ErrorCode.USER_DUPLICATED);
        }
        User user = new User(request.providerId(), request.email(), request.nickname(), UserType.EXPERT);
        userRepository.save(user);

        ExpertProfile profile = new ExpertProfile();
        profile.setUser(user);
        profile.setCategory(request.category());
        profile.setExperience(request.experience());
        profile.setStyle(request.style());
        profile.setLanguage(request.language());
        profile.setBio(request.bio());
        expertProfileRepository.save(profile);
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

    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.fromEntity(user);
    }
}
