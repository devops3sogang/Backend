package com.devops3sogang.backend.document;

import lombok.Data;
import java.util.List;

@Data
public class DailyMenu {
    private String date;
    private String dayOfWeek;
    private List<Meal> meals;
}
