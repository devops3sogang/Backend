package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder // 빌더 패턴을 사용하여 객체 생성을 용이하게 합니다.
@Schema(description = "사용자 통합 프로필 응답 DTO")
public class UserProfileResponse {

    @JsonProperty("_id")
    @Schema(description = "사용자 ID")
    private String id;

    @Schema(description = "사용자 이메일")
    private String email;

    @Schema(description = "사용자 닉네임")
    private String nickname;

    @Schema(description = "사용자 권한")
    private String role;

    @Schema(description = "계정 생성일")
    private String createdAt;

    @JsonProperty("myReviews")
    @Schema(description = "사용자가 작성한 리뷰 목록")
    private List<Review> writtenReviews;

    @Schema(description = "사용자가 '좋아요'를 누른 리뷰 목록")
    private List<Review> likedReviews;

    // User Document와 리뷰 리스트들을 받아 UserProfileResponse DTO로 변환하는 정적 메서드
    public static UserProfileResponse from(User user, List<Review> writtenReviews, List<Review> likedReviews) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .writtenReviews(writtenReviews)
                .likedReviews(likedReviews)
                .build();
    }
}