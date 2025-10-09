package com.devops3sogang.backend.document;

import lombok.Data;
import java.util.List;

@Data
public class Meal {
    private String corner;
    private String category;

    private List<String> items;
    private int price;
}