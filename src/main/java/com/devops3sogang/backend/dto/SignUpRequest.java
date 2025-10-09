package com.devops3sogang.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*; // Spring Boot 3.x

@Data
@Schema(description = "회원가입 요청 DTO") // 클래스 자체에 대한 설명
public class SignUpRequest {

    @Schema(description = "사용자 이메일", example = "user@sogang.ac.kr")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "비밀번호 (8자 이상)", example = "password1234")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @Schema(description = "사용자 닉네임", example = "서강알파카")
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;
}