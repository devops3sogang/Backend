package com.devops3sogang.backend.dto;

import com.devops3sogang.backend.document.Ratings;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ReviewRequest {

    @NotBlank(message = "리뷰 내용은 비워둘 수 없습니다.")
    private String content;

    @NotNull(message = "평점을 입력해주세요.")
    private Ratings ratings; // Document의 Ratings 보조 클래스 재사용

    private String imageUrl; // 선택 사항
}
