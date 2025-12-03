package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Type;
import com.devops3sogang.backend.document.MenuItem;
import com.devops3sogang.backend.document.RestaurantStats;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "식당 상세 정보 응답 DTO (리뷰 포함)")
public class RestaurantDetailResponse {

    @JsonProperty("_id")
    @Schema(description = "식당 ID", example = "507f1f77bcf86cd799439011")
    private String id;

    @Schema(description = "식당 이름", example = "맛있는 김밥")
    private String name;

    @Schema(description = "식당 유형", example = "OFF_CAMPUS", allowableValues = {"ON_CAMPUS", "OFF_CAMPUS"})
    private String type;

    @Schema(description = "식당 카테고리", example = "분식")
    private String category;

    @Schema(description = "식당 주소", example = "서울특별시 마포구 백범로 1")
    private String address;

    @Schema(description = "위치 정보")
    private GeoJsonPointDTO location;

    @Schema(description = "식당 이미지 URL", example = "https://example.com/kimbap.jpg")
    private String imageUrl;

    @Schema(description = "활성화 여부", example = "true")
    private boolean isActive;

    @Schema(description = "통계 정보")
    private RestaurantStats stats;

    @Schema(description = "메뉴 목록")
    private List<MenuItem> menu;

    @Schema(description = "리뷰 목록")
    private List<ReviewInfo> reviews;

    @Schema(description = "식당 정보 생성일")
    private LocalDateTime createdAt;

    @Schema(description = "식당 정보 수정일")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewInfo {
        @JsonProperty("_id")
        @Schema(description = "리뷰 ID")
        private String id;

        @Schema(description = "작성자 ID")
        private String userId;

        @Schema(description = "작성자 닉네임")
        private String nickname;

        @Schema(description = "리뷰 대상 타입 (RESTAURANT/MENU)")
        private Type targetType;

        @Schema(description = "리뷰 대상 메뉴 ID 목록")
        private List<String> menuIds;

        @Schema(description = "평점 정보")
        private RatingInfo rating;

        @Schema(description = "리뷰 내용")
        private String content;

        @Schema(description = "이미지 URL 목록")
        private List<String> imageUrls;

        @Schema(description = "좋아요 수")
        private int likeCount;

        @Schema(description = "생성일")
        private LocalDateTime createdAt;

        @JsonProperty("likedByCurrentUser")
        @Schema(description = "현재 사용자의 좋아요 여부")
        private boolean isLikedByCurrentUser;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingInfo {
        @Schema(description = "메뉴별 평점 목록")
        private List<MenuRatingInfo> menuRatings;

        @Schema(description = "식당 전체 평점 (1~5)")
        private int restaurantRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuRatingInfo {
        @Schema(description = "메뉴 ID")
        private String menuId;

        @Schema(description = "메뉴 이름")
        private String menuName;

        @Schema(description = "메뉴 평점 (1~5)")
        private int rating;
    }
}
