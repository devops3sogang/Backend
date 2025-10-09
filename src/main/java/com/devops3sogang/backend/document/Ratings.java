package com.devops3sogang.backend.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Ratings {
    @Schema(description = "맛 별점 (1~5)", example = "5")
    private int taste;

    @Schema(description = "가격 별점 (1~5)", example = "4")
    private int price;

    @Schema(description = "분위기 별점 (1~5)", example = "3")
    private int atmosphere;
}