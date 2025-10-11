package com.devops3sogang.backend.dto;

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
    @Schema(description = "리뷰 ID", example = "507f191e810c19729de860ea")
    private String id;

    @Schema(description = "작성자 ID", example = "507f191e810c19729de860e1")
    private String userId;

    @Schema(description = "작성자 닉네임", example = "김철수")
    private String nickname;

    @Schema(description = "식당 ID", example = "507f1f77bcf86cd799439011")
    private String restaurantId;

    @Schema(description = "식당 이름", example = "맛있는 김밥")
    private String restaurantName;

    @Schema(description = "평점 정보")
    private RatingsResponse ratings;

    @Schema(description = "리뷰 내용", example = "김밥이 정말 맛있어요! 참치가 많이 들어있습니다.")
    private String content;

    @Schema(description = "이미지 URL 목록")
    private List<String> imageUrls;

    @Schema(description = "좋아요 수", example = "15")
    private int likeCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingsResponse {
        @Schema(description = "메뉴별 평점 목록")
        private List<MenuRatingResponse> menuRatings;

        @Schema(description = "식당 전체 평점 (1~5)", example = "4")
        private int restaurantRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuRatingResponse {
        @Schema(description = "메뉴 이름", example = "참치김밥")
        private String menuName;

        @Schema(description = "메뉴 평점 (1~5)", example = "5")
        private int rating;
    }
}
