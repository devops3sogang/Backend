package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.dto.LoginRequest;
import com.devops3sogang.backend.dto.SignUpRequest;
import com.devops3sogang.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpRequest request) {
        authService.signup(request);
        return ResponseEntity.status(201).body("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String jwtToken = authService.login(request);
        // 실제로는 토큰을 Body에 담아 보냅니다.
        return ResponseEntity.ok(jwtToken);
    }
}
