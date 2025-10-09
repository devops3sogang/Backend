package com.devops3sogang.backend.service;

public interface LikeService {
    boolean toggleLike(String userId, String reviewId);
}