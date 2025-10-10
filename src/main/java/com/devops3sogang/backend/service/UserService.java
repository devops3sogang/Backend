package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.UserResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {
    UserResponse getUserProfile(String email);
    void updateUserProfile(String email, UserUpdateRequest request);
    void deleteUser(String email);
    List<Review> getLikedReviews(String email);
}