package com.devops3sogang.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "사용자 정보 수정 요청 DTO")
public class UserUpdateRequest {

    @Schema(description = "새 닉네임 (변경 원할 시)")
    private String nickname;

    @Schema(description = "새 비밀번호 (변경 원할 시, 8자 이상)")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;
}