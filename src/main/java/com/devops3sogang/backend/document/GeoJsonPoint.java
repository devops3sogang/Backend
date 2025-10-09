package com.devops3sogang.backend.document;

import lombok.Data;

@Data
public class GeoJsonPoint {
    private final String type = "Point";
    private double[] coordinates;
}