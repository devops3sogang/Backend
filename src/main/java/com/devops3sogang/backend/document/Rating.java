package com.devops3sogang.backend.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class Rating {
    private List<MenuRating> menuRatings;
    private int restaurantRating;
    @Data
    public static class MenuRating {
        private String menuId;
        private int rating;
    }
}