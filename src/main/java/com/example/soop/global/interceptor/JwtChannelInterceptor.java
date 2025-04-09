package com.example.soop.global.interceptor;

import com.example.soop.domain.user.User;
import com.example.soop.domain.user.UserRepository;
import com.example.soop.global.code.ErrorCode;
import com.example.soop.global.exception.UserException;
import com.example.soop.global.jwt.JwtProvider;
import com.example.soop.global.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info("connection request, interceptor,  preSend method!");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.info("sessionId: {}", accessor.getSessionId());

        // 연결 요청 시 JWT 검증
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Authorization 헤더 추출
            List<String> authorization = accessor.getNativeHeader("Authorization");
            log.info("authorization: {}", authorization.get(0));
            if (authorization != null && !authorization.isEmpty()) {
                String jwt = authorization.get(0).substring(7);
                // JWT 검증
                Long userId = Long.parseLong(jwtProvider.getUserId(jwt));
                // 사용자 정보 조회
                User user = userRepository.findById(userId).orElseThrow(() -> new UserException(
                    ErrorCode.USER_NOT_FOUND));
                // 사용자 인증 정보 설정
                CustomUserDetails customUserDetail = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    customUserDetail, null, customUserDetail.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                accessor.getSessionAttributes().put("userId", customUserDetail.getId()); // 웹소켓 세션에 유저 정보 저장 - 웹소켓 세션 유지되는 동안 계속 조회 가능
            } else{
                log.error("Authorization header is not found");
                return null; // 헤더가 없을 경우 메시지 중단
            }
        }
        return message;
    }
}
