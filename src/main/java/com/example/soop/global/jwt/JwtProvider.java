package com.example.soop.global.jwt;

import com.example.soop.domain.user.RefreshToken;
import com.example.soop.domain.user.RefreshTokenRepository;
import com.example.soop.domain.user.res.RefreshTokenResponse;
import com.example.soop.global.code.ErrorCode;
import com.example.soop.global.exception.JwtAuthenticationException;
import com.example.soop.global.exception.RefreshTokenException;
import com.example.soop.global.security.CustomUserDetails;
import com.example.soop.global.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private final Long accessValidityInSecs = 86400000L; // 1일 (24시간)
    private final Long refreshValidityInSecs = 7L * 86400000L; // 7일 (1주일)

    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    // 엑세스 토큰 발급
    public String createAccessToken(Long userId) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessValidityInSecs);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    // 리프레시 토큰 발급
    public String createRefreshToken() {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshValidityInSecs);

        return Jwts.builder()
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    public String getUserId(String token) {
        return getClaims(token).getSubject(); // 유저의 ID를 String 형태로 반환
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (SignatureException e) {
            throw new JwtAuthenticationException(ErrorCode.ACCESS_TOKEN_SIGNATURE_INVALID);
        } catch (MalformedJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.ACCESS_TOKEN_MALFORMED);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.ACCESS_TOKEN_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            throw new JwtAuthenticationException(ErrorCode.ACCESS_TOKEN_ILLEGAL_ARGUMENT);
        }
    }

    public String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer "를 제외하고 나머지 값만 반환
        }
        throw new JwtAuthenticationException(ErrorCode.ACCESS_TOKEN_ILLEGAL_ARGUMENT);
    }

    public Authentication getAuthentication(String token) {
        CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(
            getUserId(token)
        );

        return new UsernamePasswordAuthenticationToken(customUserDetails,
            null);
    }

    public boolean isRefreshTokenExpired(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey) // 서명 키 설정
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

            Date expiration = claims.getExpiration();
            return expiration.before(new Date()); // 만료 시간이 현재 시간보다 이전이면 만료됨
        } catch (Exception e) {
            return true; // 파싱 오류가 발생하면 만료된 것으로 간주
        }
    }

    public RefreshTokenResponse recreateAccessToken(String refreshToken) {
        // 유저에게 등록된 리프레시 토큰인지 확인
        RefreshToken existsRefreshToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(
                () -> new RefreshTokenException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        if (!existsRefreshToken.equals(refreshToken)) {
            throw new RefreshTokenException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        // 만료된 리프레시 토큰인지 확인
        if (isRefreshTokenExpired(refreshToken)) {
            throw new RefreshTokenException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        // 해당 리프레시 토큰의 유저 정보를 통해 다시 엑세스 토큰 생성
        String accessToken = createAccessToken(Long.valueOf(existsRefreshToken.getUser().getId()));
        return new RefreshTokenResponse(accessToken);
    }

    // 엑세스 토큰 유효성 검사
    public void validateAccessToken(String token) {
        getClaims(token);
    }
}