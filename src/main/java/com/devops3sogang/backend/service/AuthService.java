package com.devops3sogang.backend.service;

import com.devops3sogang.backend.dto.RegisterRequest;
import com.devops3sogang.backend.dto.RegisterResponse;
import com.devops3sogang.backend.dto.LoginRequest;
import com.devops3sogang.backend.dto.LoginResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    void logout(String userEmail, String accessToken);
}
