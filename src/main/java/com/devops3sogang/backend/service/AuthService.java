package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.LoginRequest;
import com.devops3sogang.backend.dto.RegisterRequest;

public interface AuthService {
    User register(RegisterRequest request);
    String login(LoginRequest request); // 로그인 성공 시 JWT 토큰 반환
}
