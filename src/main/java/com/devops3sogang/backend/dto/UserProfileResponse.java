package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder // 빌더 패턴을 사용하여 객체 생성을 용이하게 합니다.
@Schema(description = "사용자 통합 프로필 응답 DTO")
public class UserProfileResponse {

    @Schema(description = "사용자 기본 정보")
    private String id;
    private String email;
    private String nickname;

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
                .writtenReviews(writtenReviews)
                .likedReviews(likedReviews)
                .build();
    }
}