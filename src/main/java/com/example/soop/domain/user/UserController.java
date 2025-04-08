package com.example.soop.domain.user;

import com.example.soop.domain.user.req.LoginRequest;
import com.example.soop.domain.user.req.SignupRequest;
import com.example.soop.domain.user.res.RefreshTokenResponse;
import com.example.soop.global.format.ApiResponse;
import com.example.soop.global.jwt.JwtProvider;
import com.example.soop.global.jwt.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원 가입")
    @PostMapping("/signup")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "USER401", description = "이미 존재하는 유저입니다."),
    })
    public ApiResponse<String> signup(@RequestBody SignupRequest request) {
        userService.signup(request);
        return ApiResponse.createSuccess("회원 가입에 성공했습니다.");
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "USER402", description = "존재하지 않는 유저입니다."),
    })
    public ApiResponse<TokenResponse> signup(@RequestBody LoginRequest request) {
        TokenResponse tokenResponse = userService.login(request);
        return ApiResponse.createSuccessWithData(tokenResponse,"로그인에 성공했습니다.");
    }

    @PostMapping("/refresh")
    @Operation(summary = "엑세스 토큰 재발급")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "REFRESH401", description = "리프레시 토큰이 DB에 존재하지 않습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "REFRESH402", description = "해당 유저에 등록된 리프레시과 일치하지 않습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "REFRESH403", description = "리프레시 토큰이 만료되었습니다."),
    })
    public ApiResponse<RefreshTokenResponse> recreateAccessToken(
        @RequestHeader("Refresh-Token") String refreshToken
    ) {
        RefreshTokenResponse refreshTokenResponse = jwtProvider.recreateAccessToken(refreshToken);
        return ApiResponse.createSuccessWithData(refreshTokenResponse,
            "엑세스 토큰이 재발급 되었습니다.");
    }

}
