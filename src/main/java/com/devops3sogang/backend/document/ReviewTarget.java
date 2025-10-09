package com.devops3sogang.backend.document;

import lombok.Data;

@Data
public class ReviewTarget {
    private String type;
    private String restaurantId;
    private String restaurantName;
    private String menuItems;
}