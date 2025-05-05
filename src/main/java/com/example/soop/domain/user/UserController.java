package com.example.soop.domain.user;

import com.example.soop.domain.user.req.BaseSignupRequest;
import com.example.soop.domain.user.req.ExpertSignupRequest;
import com.example.soop.domain.user.req.LoginRequest;
import com.example.soop.domain.user.req.GeneralSignupRequest;
import com.example.soop.domain.user.res.ExpertUserResponse;
import com.example.soop.domain.user.res.RefreshTokenResponse;
import com.example.soop.domain.user.res.GeneralUserResponse;
import com.example.soop.domain.user.type.Category;
import com.example.soop.domain.user.type.Language;
import com.example.soop.domain.user.type.Style;
import com.example.soop.global.format.ApiResponse;
import com.example.soop.global.jwt.JwtProvider;
import com.example.soop.global.jwt.TokenResponse;
import com.example.soop.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResponse<String> signup(@RequestBody BaseSignupRequest request) {
        if (request instanceof ExpertSignupRequest expertRequest) {
            userService.signupExpert(expertRequest);
        }
        if (request instanceof GeneralSignupRequest generalRequest) {
            userService.signupGeneral(generalRequest);
        }
        return ApiResponse.createSuccess("회원 가입에 성공했습니다.");
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "USER402", description = "존재하지 않는 유저입니다."),
    })
    public ApiResponse<TokenResponse> signup(@RequestBody LoginRequest request) {
        TokenResponse tokenResponse = userService.login(request);
        return ApiResponse.createSuccessWithData(tokenResponse, "로그인에 성공했습니다.");
    }

    @Operation(summary = "엑세스 토큰 재발급")
    @PostMapping("/refresh")
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

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "USER402", description = "존재하지 않는 유저입니다."),
    })
    public ApiResponse<GeneralUserResponse> getMyInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GeneralUserResponse generalUserResponse = userService.getUserInfo(userDetails.getId());
        return ApiResponse.createSuccessWithData(generalUserResponse, "내 정보 조회에 성공했습니다.");
    }

    @Operation(summary = "전문가 필터 조회")
    @GetMapping("/experts")
    public ApiResponse<List<ExpertUserResponse>> getExperts(
        @RequestParam(required = false) Category category,
        @RequestParam(required = false) List<Style> styles,
        @RequestParam(required = false) Language language,
        @RequestParam(required = false) Integer minExperience
    ) {
        List<ExpertUserResponse> experts = userService.findExpertsByFilter(category, styles, language, minExperience);
        return ApiResponse.createSuccessWithData(experts, "전문가 목록 조회에 성공했습니다.");
    }


}
