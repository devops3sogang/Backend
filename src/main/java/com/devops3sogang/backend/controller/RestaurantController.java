package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.dto.RestaurantRequest;
import com.devops3sogang.backend.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
public class RestaurantController {

    private final RestaurantService restaurantService;

    /**
     * 맛집 목록 조회 (필터링 가능)
     * GET /api/restaurants?type=OFF_CAMPUS&category=한식
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
     * GET /api/restaurants/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable("id") String id) {
        Restaurant restaurant = restaurantService.findRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    /**
     * (관리자) 신규 맛집 등록
     * POST /api/restaurants
     */
    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody RestaurantRequest request) {
        Restaurant newRestaurant = restaurantService.create(request);
        return ResponseEntity.status(201).body(newRestaurant); // 201 Created
    }
}