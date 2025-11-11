package com.devops3sogang.backend.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

@Data
@Document(collection = "restaurants")
@CompoundIndexes({
    @CompoundIndex(name = "name_address_idx", def = "{'name': 1, 'address': 1}", unique = true)
})
public class Restaurant {
    @Id
    private String id;
    private String name;
    private String type;
    private String category;
    private String address;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;
    private String imageUrl;
    @JsonProperty("isActive")
    private boolean isActive;
    private RestaurantStats stats;
    private List<MenuItem> menu;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 거리 기반 검색 시에만 사용되는 필드 (DB에는 저장되지 않음)
    @Transient
    private Double distance;
}