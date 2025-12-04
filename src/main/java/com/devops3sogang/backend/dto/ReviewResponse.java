package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Type;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 응답 DTO")
public class ReviewResponse {

    @JsonProperty("_id")
    @Schema(description = "리뷰 ID")
    private String id;

    @Schema(description = "작성자 ID")
    private String userId;

    @Schema(description = "작성자 닉네임")
    private String nickname;

    @Schema(description = "식당 ID")
    private String restaurantId;

    @Schema(description = "식당 이름")
    private String restaurantName;

    @Schema(description = "리뷰 대상 타입 (RESTAURANT/MENU)")
    private Type targetType;
    
    @Schema(description = "메뉴 ID 목록")
    private List<String> menuIds;

    @Schema(description = "평점 정보")
    private RatingsResponse ratings;

    @Schema(description = "리뷰 내용")
    private String content;

    @Schema(description = "이미지 URL 목록")
    private List<String> imageUrls;

    @Schema(description = "좋아요 수")
    private int likeCount;

    @Schema(description = "리뷰 작성일")
    private String createdAt;

    @Schema(description = "리뷰 수정일")
    private String updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingsResponse {
        @Schema(description = "메뉴별 평점")
        private List<MenuRatingResponse> menuRatings;

        @Schema(description = "식당 전체 평점 (1~5)")
        private int restaurantRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuRatingResponse {
        @Schema(description = "메뉴 ID")
        private String menuId;

        @Schema(description = "메뉴 이름")
        private String menuName;

        @Schema(description = "메뉴 평점 (1~5)")
        private int rating;
    }
}
