package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.RestaurantRequest;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.service.RestaurantService;
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
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant Controller", description = "맛집 전용 API")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final ReviewService reviewService;

    /**
     * 맛집 목록 조회 (필터링 가능)
     * GET /restaurants?type=OFF_CAMPUS&category=한식
     */
    @GetMapping
    public ResponseEntity<List<Restaurant>> getRestaurants(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "category", required = false) String category) {
        List<Restaurant> restaurants = restaurantService.findRestaurants(type, category);
        return ResponseEntity.ok(restaurants);
    }

    /**
     * 맛집 상세 정보 조회
     * GET /restaurants/{restaurantId}
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<Restaurant> getRestaurantById(
            @PathVariable("restaurantId") String restaurantId) {
        Restaurant restaurant = restaurantService.findRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant);
    }

    /**
     * (관리자) 신규 맛집 등록
     * POST /restaurants
     */
    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Restaurant> createRestaurant(
            @Valid @RequestBody RestaurantRequest request) {  // @Valid 추가됨
        Restaurant newRestaurant = restaurantService.create(request);
        return ResponseEntity.status(201).body(newRestaurant);  // 201 Created
    }

    /**
     * (관리자) 식당 정보 수정
     * PUT /restaurants/{restaurantId}
     */
    @PutMapping("/{restaurantId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Restaurant> updateRestaurant(
            @PathVariable("restaurantId") String restaurantId,
            @Valid @RequestBody RestaurantRequest request) {
        Restaurant updatedRestaurant = restaurantService.update(restaurantId, request);
        return ResponseEntity.ok(updatedRestaurant);
    }

    /**
     * 특정 맛집에 리뷰 작성
     * POST /restaurants/{restaurantId}/reviews
     */
    @PostMapping("/{restaurantId}/reviews")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Review> createReview(
            @PathVariable("restaurantId") String restaurantId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        Review newReview = reviewService.createReview(userEmail, restaurantId, request);
        return ResponseEntity.status(201).body(newReview);
    }

    /**
     * (관리자) 맛집 삭제
     * DELETE /restaurants/{restaurantId}
     */
    @DeleteMapping("/{restaurantId}")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> deleteRestaurant(
            @PathVariable("restaurantId") String restaurantId) {
        restaurantService.deleteRestaurant(restaurantId);
        return ResponseEntity.status(204).body(null);  // 204 No Content
    }
}