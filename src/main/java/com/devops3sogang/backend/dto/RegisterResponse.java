package com.devops3sogang.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "회원가입 응답 DTO")
public class RegisterResponse {

    @Schema(description = "사용자 ID", example = "12")
    private String id;

    @Schema(description = "등록된 사용자 이메일", example = "testuser1@sogang.ac.kr")
    private String email;

    @Schema(description = "등록된 사용자 닉네임", example = "서강알로스")
    private String nickname;

    @Schema(description = "가입 완료 메시지", example = "회원가입이 완료되었습니다.")
    private String message;

    @Schema(description = "가입 일시", example = "2025-02-23T14:30:12")
    private LocalDateTime createdAt;
}