package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.service.LikeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Like Controller", description = "좋아요 전용 API")
public class LikeController {

    private final LikeService likeService;

    /**
     * 특정 리뷰에 대한 '좋아요'를 토글(추가/취소)합니다.
     * POST /reviews/{reviewId}/like
     */
    @PostMapping("/{reviewId}/like")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> toggleLike(@PathVariable("reviewId") String reviewId, Authentication authentication) {
        // 인증된 사용자 정보에서 UserDetails를 가져옴
        User userDetails = (User) authentication.getPrincipal();
        String userId = userDetails.getId(); // User의 고유 ID

        boolean isLiked = likeService.toggleLike(userId, reviewId);

        LikeResponse response = new LikeResponse(isLiked);
        return ResponseEntity.ok(response);
    }
}