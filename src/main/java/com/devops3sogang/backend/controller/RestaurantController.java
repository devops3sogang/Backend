package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.MenuItem;
import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.SortBy;
import com.devops3sogang.backend.dto.RestaurantDetailResponse;
import com.devops3sogang.backend.dto.RestaurantSearchRequest;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.service.RestaurantService;
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
@Tag(name = "Restaurant Controller", description = "식당 전용 API")
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final RestaurantRepository restaurantRepository;

    /**
     * 맛집 목록 조회 (필터링 및 거리 기반 검색 가능)
     * GET /restaurants?type=OFF_CAMPUS&category=한식&lat=37.123&lng=127.123&radius=500&sortby=NONE
     */
    @GetMapping
    public ResponseEntity<List<Restaurant>> getRestaurants(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "lat", required = false) Double latitude, //null 아닌 400으로 직접 처리 위해 required 해제
            @RequestParam(name = "lng", required = false) Double longitude,
            @RequestParam(name = "radius", required = false) Integer radius,
            @RequestParam(name = "sortBy", required = false, defaultValue = "NONE") String sortByStr) {

        SortBy sortBy;
        try {
            sortBy = SortBy.valueOf(sortByStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sortBy = SortBy.NONE; // 기본값 처리
        }

        if (latitude == null || longitude == null) {
            return ResponseEntity.badRequest().build();
        }

        // DTO 생성
        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setType(type);
        request.setCategory(category);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setRadius(radius);
        request.setSortBy(sortBy);

        List<Restaurant> restaurants = restaurantService.findRestaurants(request);

        return ResponseEntity.ok(restaurants);
    }

    /**
     * 맛집 상세 정보 조회 (리뷰 포함)
     * GET /restaurants/{restaurantId}
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDetailResponse> getRestaurantById(
            @PathVariable("restaurantId") String restaurantId,
            Authentication authentication) {
        // 현재 로그인한 사용자의 ID 가져오기 (로그인하지 않은 경우 null)
        String currentUserId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            // User 객체의 MongoDB ID를 가져옴 (좋아요 비교에 사용)
            com.devops3sogang.backend.document.User userDetails =
                (com.devops3sogang.backend.document.User) authentication.getPrincipal();
            currentUserId = userDetails.getId();
        }

        RestaurantDetailResponse response = restaurantService.findRestaurantDetailById(restaurantId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<List<MenuItem>> getMenu(@PathVariable String restaurantId) {
        return restaurantRepository.findById(restaurantId)
            .map(restaurant -> ResponseEntity.ok(restaurant.getMenu()))
            .orElse(ResponseEntity.notFound().build());
    }
}