package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Type;
import com.devops3sogang.backend.document.Rating;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@Schema(description = "리뷰 작성 요청 DTO")
public class ReviewRequest {

    @Schema(description = "리뷰 대상 식당 ID (필수, 교내 식당은 'MAIN_CAMPUS')")
    @NotBlank(message = "restaurantId를 입력해주세요.")
    private String restaurantId;

    @Schema(description = "리뷰 대상 타입 (RESTAURANT/MENU)")
    @NotNull(message = "리뷰 대상 타입을 선택해주세요.")
    private Type targetType;

    @Schema(description = "리뷰 대상 메뉴 ID 목록 (Type.MENU인 경우 단일 메뉴 ID 사용)")
    private List<String> menuIds;

    @Schema(description = "리뷰 본문")
    private String content;

    @Schema(description = "세부 평점 (Type.RESTAURANT 리뷰에서 메뉴를 선택하면, menuRatings에 선택한 메뉴 ID에 대한 평점이 있어야 함)")
    @NotNull(message = "평점을 입력해주세요.")
    private Rating rating;

    @Schema(description = "첨부 이미지 URL 목록")
    private List<String> imageUrls;
}