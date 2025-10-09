package com.devops3sogang.backend.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MenuItem {
    @Schema(description = "메뉴 이름", example = "제육볶음")
    private String name;

    @Schema(description = "메뉴 가격", example = "8000")
    private int price;
}