package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 특정 맛집의 모든 리뷰 조회
     * GET /api/restaurants/{restaurantId}/reviews
     */
    @GetMapping("/restaurants/{restaurantId}/reviews")
    public ResponseEntity<List<Review>> getReviewsByRestaurant(@PathVariable("restaurantId") String restaurantId) {
        List<Review> reviews = reviewService.findReviewsByRestaurantId(restaurantId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 특정 맛집에 리뷰 작성
     * POST /api/restaurants/{restaurantId}/reviews
     */
    @PostMapping("/restaurants/{restaurantId}/reviews")
    public ResponseEntity<Review> createReview(
            @PathVariable("restaurantId") String restaurantId,
            @RequestBody ReviewRequest request,
            Authentication authentication) { // 👈 Authentication 객체를 파라미터로 추가

        // Authentication 객체에서 사용자의 email(우리의 username)을 가져옵니다.
        String userEmail = authentication.getName();

        // 이제 email을 사용하여 리뷰를 생성합니다. (Service 계층도 약간 수정이 필요합니다)
        Review newReview = reviewService.createReview(userEmail, restaurantId, request);
        return ResponseEntity.status(201).body(newReview);
    }
}