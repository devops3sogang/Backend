package com.devops3sogang.backend.service;

import com.devops3sogang.backend.dto.UserProfileResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;

public interface UserService {
    UserProfileResponse getComprehensiveUserProfile(String email);
    void updateUserProfile(String email, UserUpdateRequest request);
    void deleteUser(String email);
}