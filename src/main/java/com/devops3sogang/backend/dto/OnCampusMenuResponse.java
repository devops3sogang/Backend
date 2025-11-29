package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.DailyMenu;

import lombok.Data;

import java.util.List;

@Data
public class OnCampusMenuResponse {
    private String id;
    private String restaurantId;
    private String restaurantName;
    private String weekStartDate;
    private List<DailyMenu> dailyMenus;
}