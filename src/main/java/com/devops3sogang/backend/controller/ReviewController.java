package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.dto.ReviewUpdateRequest;
import com.devops3sogang.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 특정 맛집의 모든 리뷰 조회
     * GET /reviews?restaurantId={restaurantId}
     */
    @GetMapping
    public ResponseEntity<List<Review>> getReviewsByRestaurant(@RequestParam("restaurantId") String restaurantId) {
        List<Review> reviews = reviewService.findReviewsByRestaurantId(restaurantId);
        return ResponseEntity.ok(reviews);
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
        return ResponseEntity.ok().build();
    }
}