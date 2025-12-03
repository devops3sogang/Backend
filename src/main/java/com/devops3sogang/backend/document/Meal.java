package com.devops3sogang.backend.document;

import lombok.Data;
import java.util.List;

@Data
public class Meal {
    private String category;
    private List<MenuItem> items;
    private int price;
}