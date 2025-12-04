package com.devops3sogang.backend.document;

import lombok.Data;
import java.util.List;

@Data
public class ReviewTarget {
    private Type type;
    private String restaurantId;
    private List<String> menuIds;
}