package com.devops3sogang.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@Schema(description = "사용자 정보 수정 응답 DTO")
public class UserUpdateResponse {

    @JsonProperty("_id")
    @Schema(description = "사용자 ID", example = "507f191e810c19729de860e1")
    private String id;

    @Schema(description = "사용자 이메일", example = "user1@sogang.ac.kr")
    private String email;

    @Schema(description = "변경된 닉네임", example = "서강대김철수")
    private String nickname;

    @Schema(description = "마지막 정보 수정일", example = "2025-10-10T18:30:00Z")
    private String updatedAt;

    public static UserUpdateResponse from(String id, String email, String nickname, LocalDateTime updatedAt) {
        return UserUpdateResponse.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .updatedAt(updatedAt.format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}