package com.devops3sogang.backend.service;

import com.devops3sogang.backend.dto.UserResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;

public interface UserService {
    UserResponse getUserProfile(String email);
    void updateUserProfile(String email, UserUpdateRequest request);
    void deleteUser(String email);
}