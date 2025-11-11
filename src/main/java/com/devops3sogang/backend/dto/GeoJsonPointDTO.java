package com.devops3sogang.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GeoJsonPointDTO {
    @Schema(description = "GeoJSON 타입", example = "Point")
    private final String type = "Point";

    @Schema(description = "좌표 [경도, 위도]", example = "[126.9410, 37.5509]")
    private double[] coordinates;
}
