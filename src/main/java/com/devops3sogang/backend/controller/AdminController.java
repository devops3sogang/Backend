package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/restaurants")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "관리자 전용 API")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final RestaurantService restaurantService;

    /**
     * (관리자) 특정 맛집을 ID로 삭제합니다.
     * (관련된 리뷰, 좋아요 정보도 함께 삭제됩니다.)
     */
    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable("restaurantId") String restaurantId) {
        restaurantService.deleteRestaurant(restaurantId);
        return ResponseEntity.status(204).build();  // ← 204 No Content
    }
}
