package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.SortBy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "식당 검색 요청")
public class RestaurantSearchRequest {
  // required
  @Schema(description = "기준 위도")
  @NotNull
  private Double latitude;
  @Schema(description = "기준 경도")
  @NotNull
  private Double longitude;

  //optional
  @Schema(description = "식당 유형")
  private String type;
  @Schema(description = "카테고리 필터")
  private String category;
  @Schema(description = "거리 필터 (단위:m)")
  private Integer radius;
  @Schema(description = "정렬 기준 (NONE/DISTANCE/RATING)")
  private SortBy sortBy;

  public void setType(String type) { this.type = type; }
}