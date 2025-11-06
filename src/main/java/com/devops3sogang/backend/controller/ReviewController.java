package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.dto.ReviewResponse;
import com.devops3sogang.backend.dto.ReviewUpdateRequest;
import com.devops3sogang.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Controller", description = "리뷰 전용 API")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 목록 조회
     * - restaurantId 있으면: 특정 맛집의 리뷰 조회
     * - restaurantId 없으면: 전체 리뷰 조회 (최신순 5개)
     * GET /reviews?restaurantId={restaurantId}
     * GET /reviews
     */
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(
        @RequestParam(name = "restaurantId", required = false) String restaurantId,
        @RequestParam(name = "limit", required = false, defaultValue = "5") int limit) {

        List<Review> reviews;

        if (restaurantId != null) {
            // 특정 식당의 리뷰 조회
            reviews = reviewService.findReviewsByRestaurantId(restaurantId);
        } else {
            // 전체 리뷰 조회 (최신순)
            reviews = reviewService.findRecentReviews(limit);
        }

        // Review 엔티티를 ReviewResponse DTO로 변환
        List<ReviewResponse> response = reviews.stream()
            .map(this::convertToResponse)
            .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 작성
     * POST /reviews
     */
    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Review> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        Review newReview = reviewService.createReview(userEmail, request.getRestaurantId(), request);
        return ResponseEntity.status(201).body(newReview);
    }

    /**
     * Review 엔티티를 ReviewResponse DTO로 변환
     */
    private ReviewResponse convertToResponse(Review review) {
        // menuRatings 변환 (null 체크 추가)
        List<ReviewResponse.MenuRatingResponse> menuRatings = List.of();
        if (review.getRatings() != null && review.getRatings().getMenuRatings() != null) {
            menuRatings = review.getRatings().getMenuRatings().stream()
                .map(mr -> ReviewResponse.MenuRatingResponse.builder()
                    .menuName(mr.getMenuName())
                    .rating(mr.getRating())
                    .build())
                .toList();
        }

        // RatingsResponse 생성
        int restaurantRating = (review.getRatings() != null) ? review.getRatings().getRestaurantRating() : 0;
        ReviewResponse.RatingsResponse ratingsResponse = ReviewResponse.RatingsResponse.builder()
            .menuRatings(menuRatings)
            .restaurantRating(restaurantRating)
            .build();

        // imageUrls 처리
        List<String> imageUrls = review.getImageUrls() != null ? review.getImageUrls() : List.of();

        return ReviewResponse.builder()
            .id(review.getId())
            .userId(review.getUserId())
            .nickname(review.getNickname())
            .restaurantId(review.getTarget().getRestaurantId())
            .restaurantName(review.getTarget().getRestaurantName())
            .ratings(ratingsResponse)
            .content(review.getContent())
            .imageUrls(imageUrls)
            .likeCount(review.getLikeCount())
            .build();
    }

    /**
     * 리뷰 수정
     * PUT /reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Review> updateReview(
            @PathVariable("reviewId") String reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        Review updatedReview = reviewService.updateReview(reviewId, request, userEmail);
        return ResponseEntity.ok(updatedReview);
    }

    /**
     * 리뷰 삭제
     * DELETE /review/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> deleteReview(
        @PathVariable("reviewId") String reviewId,
        Authentication authentication) {
        String userEmail = authentication.getName();
        reviewService.deleteReview(reviewId, userEmail);
        return ResponseEntity.noContent().build();
    }
}