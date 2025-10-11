package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.dto.ReviewUpdateRequest;

import java.util.List;

public interface ReviewService {
    Review createReview(String userEmail, String restaurantId, ReviewRequest request);
    List<Review> findReviewsByRestaurantId(String restaurantId);
    List<Review> findRecentReviews(int limit);
    Review updateReview(String reviewId, ReviewUpdateRequest request, String userEmail);
    void deleteReview(String reviewId, String userEmail);
}
