package com.devops3sogang.backend.document;

import lombok.Data;

@Data
public class RestaurantStats {
    private double rating;
    private int reviewCount;
    private int likeCount;
}