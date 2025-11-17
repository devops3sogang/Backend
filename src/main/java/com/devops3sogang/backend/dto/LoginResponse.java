package com.devops3sogang.backend.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "로그인 응답 DTO")
public class LoginResponse {

    @Schema(description = "JWT 인증 토큰")
    private String token;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "유저 이메일")
    private String email;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "토큰 만료 시간 (epoch millis)", example = "1731842130000")
    private Long expiresAt;
}