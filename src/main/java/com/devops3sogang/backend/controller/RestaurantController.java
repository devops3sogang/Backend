package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.RestaurantRequest;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.service.RestaurantService;
import com.devops3sogang.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
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
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable("restaurantId") String restaurantId) {
        Restaurant restaurant = restaurantService.findRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant);
    }

    /**
     * (관리자) 신규 맛집 등록
     * POST /restaurants
     */
    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody RestaurantRequest request) {
        Restaurant newRestaurant = restaurantService.create(request);
        return ResponseEntity.status(201).body(newRestaurant); // 201 Created
    }

    /**
     * 특정 맛집에 리뷰 작성
     * POST /restaurants/{restaurantId}/reviews
     */
    @PostMapping("/{restaurantId}/reviews")
    public ResponseEntity<Review> createReview(
            @PathVariable("restaurantId") String restaurantId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        Review newReview = reviewService.createReview(userEmail, restaurantId, request);
        return ResponseEntity.status(201).body(newReview);
    }
}