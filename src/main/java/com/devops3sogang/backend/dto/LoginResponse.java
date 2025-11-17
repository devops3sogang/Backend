package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Role;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "로그인 응답 DTO")
public class LoginResponse {
    @Schema(description = "JWT 인증 토큰")
    private String token;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "유저 정보")
    private UserInfo user;

    @Schema(description = "토큰 만료 시간 (epoch millis)", example = "1731842130000")
    private Long expiresAt;

    @Data
    @Schema(description = "유저 정보 DTO")
    public static class UserInfo {
        private String _id;
        private String email;
        private String nickname;
        private Role role;
    }
}