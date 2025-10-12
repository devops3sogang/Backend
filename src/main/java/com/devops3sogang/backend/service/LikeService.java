package com.devops3sogang.backend.service;

import com.devops3sogang.backend.dto.CreateLikeResponse;

public interface LikeService {
    CreateLikeResponse toggleLike(String userId, String reviewId);
}