package com.devops3sogang.backend.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class Rating {
    @Schema(description = "메뉴별 평점 목록")
    private List<MenuRating> menuRatings;

    @Schema(description = "식당 전체 평점 (1~5)", example = "4")
    private int restaurantRating;

    @Data
    public static class MenuRating {
        @Schema(description = "메뉴 이름", example = "참치김밥")
        private String menuName;

        @Schema(description = "메뉴 평점 (1~5)", example = "5")
        private int rating;
    }
}