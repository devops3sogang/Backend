package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.dto.RestaurantRequest;
import com.devops3sogang.backend.service.RestaurantService;
import com.devops3sogang.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin") // Changed base path to /admin
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "관리자 전용 API")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final RestaurantService restaurantService;
    private final ReviewService reviewService; // Injected ReviewService

    /**
     * (관리자) 신규 맛집을 등록
     * POST /admin/restaurants
     */
    @PostMapping("/restaurants")
    public ResponseEntity<Restaurant> createRestaurant(
            @Valid @RequestBody RestaurantRequest request) {
        Restaurant newRestaurant = restaurantService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRestaurant);
    }

    /**
     * (관리자) 특정 맛집 정보를 수정
     * PUT /admin/restaurants/{restaurantId}
     */
    @PutMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Restaurant> updateRestaurant(
            @PathVariable("restaurantId") String restaurantId,
            @Valid @RequestBody RestaurantRequest request) {
        Restaurant updatedRestaurant = restaurantService.update(restaurantId, request);
        return ResponseEntity.ok(updatedRestaurant);
    }

    /**
     * (관리자) 특정 맛집을 ID로 삭제
     * (관련된 리뷰, 좋아요 정보도 함께 삭제)
     * DELETE /admin/restaurants/{restaurantId}
     */
    @DeleteMapping("/restaurants/{restaurantId}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable("restaurantId") String restaurantId) {
        restaurantService.deleteRestaurant(restaurantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * (관리자) 특정 리뷰를 ID로 삭제
     * DELETE /admin/reviews/{reviewId}
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("reviewId") String reviewId,
            Authentication authentication) {
        String adminEmail = authentication.getName(); // 관리자 이메일
        reviewService.deleteReview(reviewId, adminEmail);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
