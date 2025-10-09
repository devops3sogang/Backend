package com.devops3sogang.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {

    @Schema(description = "로그인 이메일", example = "user@sogang.ac.kr")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "비밀번호", example = "password1234")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
}