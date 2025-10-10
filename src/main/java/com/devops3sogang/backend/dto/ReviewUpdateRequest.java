package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Ratings;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "리뷰 수정 요청 DTO")
public class ReviewUpdateRequest {

    @Schema(description = "수정할 리뷰 본문")
    @NotBlank(message = "리뷰 내용은 비워둘 수 없습니다.")
    private String content;

    @Schema(description = "수정할 세부 평점")
    @NotNull(message = "평점을 입력해주세요.")
    private Ratings ratings;

    @Schema(description = "수정할 이미지 URL (선택 사항)")
    private String imageUrl;
}