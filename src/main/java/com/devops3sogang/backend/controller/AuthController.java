package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.dto.LoginRequest;
import com.devops3sogang.backend.dto.RegisterRequest;
import com.devops3sogang.backend.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "인증 전용 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).body("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        String jwtToken = authService.login(request);
        // 실제로는 토큰을 Body에 담아 보냅니다.
        return ResponseEntity.ok(jwtToken);
    }

    /**
     * 로그아웃
     * POST /auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // JWT 기반에서는 클라이언트 측에서 토큰을 삭제하는 것이 일반적입니다.
        // 서버에서는 특별히 할 일이 없거나, 토큰을 블랙리스트에 추가하는 로직을 구현할 수 있습니다.
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}
