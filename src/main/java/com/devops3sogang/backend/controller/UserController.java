package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.UserProfileResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;
import com.devops3sogang.backend.dto.UserUpdateResponse;
import com.devops3sogang.backend.dto.DeleteUserRequest;
import com.devops3sogang.backend.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "사용자 전용 API")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 사용자의 통합 프로필 정보를 조회합니다.
     * (프로필, 작성 리뷰, 좋아요한 리뷰 포함)
     * GET /users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        UserProfileResponse userProfile = userService.getComprehensiveUserProfile(userEmail);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 수정합니다.
     * PUT /users/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserUpdateResponse> updateMyProfile(Authentication authentication, @Valid @RequestBody UserUpdateRequest request) {
        String userEmail = authentication.getName();
        User updatedUser = userService.updateUserProfile(userEmail, request);
        UserUpdateResponse response = UserUpdateResponse.from(
            updatedUser.getId(),
            updatedUser.getEmail(),
            updatedUser.getNickname(),
            updatedUser.getUpdatedAt()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 사용자를 탈퇴 처리합니다.
     * DELETE /users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication,
                                                @Valid @RequestBody DeleteUserRequest request) {
        String userEmail = authentication.getName();
        userService.deleteUser(userEmail, request.getPassword());
        return ResponseEntity.status(204).build();  // ← 204 No Content
    } 
}
