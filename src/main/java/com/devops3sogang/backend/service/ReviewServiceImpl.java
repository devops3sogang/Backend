package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.ReviewTarget;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import com.devops3sogang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public Review createReview(String userEmail, String restaurantId, ReviewRequest request) {
        // 사용자 정보와 식당 정보가 존재하는지 확인
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("맛집 정보를 찾을 수 없습니다."));

        Review review = new Review();
        review.setUserId(user.getId());
        review.setNickname(user.getNickname()); // User Document에서 닉네임 가져오기

        ReviewTarget target = new ReviewTarget();
        target.setType("RESTAURANT");
        target.setRestaurantId(restaurantId);
        review.setTarget(target);

        review.setContent(request.getContent());
        review.setRatings(request.getRatings());
        review.setImageUrl(request.getImageUrl());
        review.setLikeCount(0);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> findReviewsByRestaurantId(String restaurantId) {
        return reviewRepository.findByTarget_RestaurantId(restaurantId);
    }
}