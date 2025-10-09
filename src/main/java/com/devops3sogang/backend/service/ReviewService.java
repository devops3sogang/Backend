package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.ReviewRequest;
import java.util.List;

public interface ReviewService {
    Review createReview(String userEmail, String restaurantId, ReviewRequest request);
    List<Review> findReviewsByRestaurantId(String restaurantId);
}
