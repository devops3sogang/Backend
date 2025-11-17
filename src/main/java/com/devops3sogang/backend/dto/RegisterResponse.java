package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "회원가입 응답 DTO")
public class RegisterResponse {

    @Schema(description = "사용자 ID", example = "12")
    private String _id;

    @Schema(description = "등록된 사용자 이메일", example = "testuser1@sogang.ac.kr")
    private String email;

    @Schema(description = "등록된 사용자 닉네임", example = "서강알로스")
    private String nickname;

    @Schema(description = "역할 (USER|ADMIN)", example = "ADMIN")
    private Role role;

    @Schema(description = "가입 일시", example = "2025-02-23T14:30:12")
    private LocalDateTime createdAt;

    @Schema(description = "최종 수정 일시", example = "2025-02-23T14:30:12")
    private LocalDateTime updatedAt;
}