package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Type;
import com.devops3sogang.backend.document.Rating;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "리뷰 수정 요청 DTO")
public class ReviewUpdateRequest {
    @Schema(description = "수정할 리뷰 대상 메뉴 ID 목록")
    private List<String> menuIds;

    @Schema(description = "수정할 리뷰 본문")
    private String content;

    @Schema(description = "수정할 세부 평점")
    @NotNull(message = "평점을 입력해주세요.")
    private Rating rating;

    @Schema(description = "수정할 이미지 URL 배열")
    private List<String> imageUrls;
}