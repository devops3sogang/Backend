package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Ratings;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "리뷰 작성 요청 DTO")
public class ReviewRequest {

    @Schema(description = "리뷰 본문 (선택 사항)", example = "여기 정말 맛있어요! 추천합니다.")
    private String content;

    @Schema(description = "세부 평점")
    @NotNull(message = "평점을 입력해주세요.")
    private Ratings ratings;

    @Schema(description = "첨부 이미지 URL 배열 (선택 사항)", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> imageUrls;
}