package com.devops3sogang.backend.document;

import lombok.Data;

@Data
public class ReviewTarget {
    private Type type;
    private String restaurantId;
    private String menuId;
}