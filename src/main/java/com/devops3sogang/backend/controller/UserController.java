package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.dto.UserResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;
import com.devops3sogang.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회합니다.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        UserResponse userProfile = userService.getUserProfile(userEmail);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 수정합니다.
     */
    @PutMapping("/me")
    public ResponseEntity<Void> updateMyProfile(Authentication authentication, @Valid @RequestBody UserUpdateRequest request) {
        String userEmail = authentication.getName();
        userService.updateUserProfile(userEmail, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 로그인한 사용자를 탈퇴 처리합니다.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        String userEmail = authentication.getName();
        userService.deleteUser(userEmail);
        return ResponseEntity.ok().build();
    }
}