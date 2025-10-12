package com.devops3sogang.backend.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "restaurants")
public class Restaurant {
    @Id
    private String id;
    private String name;
    private String type;
    private String category;
    private String address;
    private GeoJsonPoint location;
    private String imageUrl;
    @JsonProperty("isActive")
    private boolean isActive;
    private RestaurantStats stats;
    private List<MenuItem> menu;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}