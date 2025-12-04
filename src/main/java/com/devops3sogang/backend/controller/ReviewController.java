package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.ReviewTarget;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.dto.ReviewResponse;
import com.devops3sogang.backend.dto.ReviewUpdateRequest;
import com.devops3sogang.backend.service.ReviewService;
import com.devops3sogang.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Controller", description = "리뷰 전용 API")
public class ReviewController {

    private final ReviewService reviewService;
    private final RestaurantService restaurantService;

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
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        Review newReview = reviewService.createReview(userEmail, request);
        ReviewResponse response = convertToResponse(newReview);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Review 엔티티를 ReviewResponse DTO로 변환
     */
    private ReviewResponse convertToResponse(Review review) {
        // menuRatings 변환 (null 체크 추가)
        List<ReviewResponse.MenuRatingResponse> menuRatings = List.of();
        if (review.getRating() != null && review.getRating().getMenuRatings() != null) {
            Restaurant restaurant = restaurantService.findRestaurantById(review.getTarget().getRestaurantId());
            
            Map<String, String> menuNameMap = new HashMap<>();
            if (restaurant != null && restaurant.getMenu() != null) {
                restaurant.getMenu().forEach(menu -> menuNameMap.put(menu.getId(), menu.getName()));
            }
            menuRatings = review.getRating().getMenuRatings().stream()
                .map(mr -> ReviewResponse.MenuRatingResponse.builder()
                    .menuId(mr.getMenuId())
                    .menuName(menuNameMap.get(mr.getMenuId()))
                    .rating(mr.getRating())
                    .build())
                .toList();
        }

        // RatingsResponse 생성
        int restaurantRating = (review.getRating() != null) ? review.getRating().getRestaurantRating() : 0;
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
            .restaurantName(restaurantService.getRestaurantNameById(review.getTarget().getRestaurantId()))
            .ratings(ratingsResponse)
            .content(review.getContent())
            .imageUrls(imageUrls)
            .likeCount(review.getLikeCount())
            .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
            .build();
    }

    /**
     * 리뷰 수정
     * PUT /reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable("reviewId") String reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        Review updatedReview = reviewService.updateReview(reviewId, request, userEmail);
        ReviewResponse response = convertToResponse(updatedReview);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 삭제
     * DELETE /reviews/{reviewId}
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

    /**
     * 메뉴별 리뷰 조회
     * GET /reviews/by-menu
     */
    @GetMapping("/by-menu")
    public ResponseEntity<List<ReviewResponse>> getReviewsByMenu(
            @RequestParam String restaurantId,
            @RequestParam String menuId) {

        List<Review> reviews = reviewService.findReviewsByMenu(restaurantId, menuId);
        return ResponseEntity.ok(reviews.stream().map(this::convertToResponse).toList());
    }
}