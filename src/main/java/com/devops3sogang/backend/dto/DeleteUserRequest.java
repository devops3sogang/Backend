package com.devops3sogang.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "회원 탈퇴 요청 DTO")
public class DeleteUserRequest {

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "비밀번호", example = "mypassword123")
    private String password;
}