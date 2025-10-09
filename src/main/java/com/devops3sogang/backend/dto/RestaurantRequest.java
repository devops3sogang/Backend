package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.GeoJsonPoint;
import com.devops3sogang.backend.document.MenuItem;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class RestaurantRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String type; // ON_CAMPUS 또는 OFF_CAMPUS

    private String category;
    private String address;
    private GeoJsonPoint location;
    private List<MenuItem> menu; // 교외 식당 메뉴
}