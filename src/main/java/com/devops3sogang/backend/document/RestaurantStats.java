package com.devops3sogang.backend.document;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantStats {
    private double rating;
    private int reviewCount;
    private int likeCount;
}