package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.config.jwt.JwtUtil;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.LoginRequest;
import com.devops3sogang.backend.dto.LoginResponse;
import com.devops3sogang.backend.dto.RegisterRequest;
import com.devops3sogang.backend.dto.RegisterResponse;
import com.devops3sogang.backend.service.AuthService;
import com.devops3sogang.backend.service.RefreshTokenService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "인증 전용 API")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal User user,
                                         @RequestHeader("Authorization") String authHeader) {

        String accessToken = authHeader.replace("Bearer ", "");
        authService.logout(user.getEmail(), accessToken);

        return ResponseEntity.ok("로그아웃 완료");
    }

    /**
     * Access Token 재발급 (Refresh Token 사용)
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        var tokenRecord = refreshTokenService.validateRefreshToken(refreshToken);
        if (tokenRecord == null || tokenRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        refreshTokenService.revoke(tokenRecord);

        String email = tokenRecord.getEmail();
        String newAccess = jwtUtil.createToken(email);
        String newRefresh = refreshTokenService.generateRefreshToken(email);

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccess,
                "refreshToken", newRefresh
        ));
    }
}