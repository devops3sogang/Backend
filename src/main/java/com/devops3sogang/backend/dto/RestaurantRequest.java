package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.GeoJsonPoint;
import com.devops3sogang.backend.document.MenuItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
@Schema(description = "맛집 등록 요청 DTO")
public class RestaurantRequest {

    @Schema(description = "식당 이름", example = "거구장")
    @NotBlank
    private String name;

    @Schema(description = "식당 타입 (캠퍼스 내부/외부)", example = "OFF_CAMPUS", allowableValues = { "ON_CAMPUS", "OFF_CAMPUS" })
    @NotBlank
    private String type; // ON_CAMPUS 또는 OFF_CAMPUS

    @Schema(description = "음식 카테고리", example = "한식")
    @NotBlank
    private String category;

    @Schema(description = "식당 주소", example = "서울특별시 마포구 백범로 17")
    @NotBlank
    private String address;

    @Schema(description = "식당 위치 정보 (GeoJSON)")
    @NotNull
    private GeoJsonPoint location;

    @Schema(description = "식당 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "교외 식당 메뉴 목록 (선택 사항)")
    private List<MenuItem> menu; // 교외 식당 메뉴
}