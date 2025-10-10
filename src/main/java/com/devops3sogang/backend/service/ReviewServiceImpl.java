package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.ReviewTarget;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.dto.ReviewUpdateRequest;
import com.devops3sogang.backend.repository.LikeRepository;
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
    private final LikeRepository likeRepository;

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

    @Override
    public Review updateReview(String reviewId, ReviewUpdateRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        // 리뷰 작성자와 수정 요청자가 동일인인지 확인
        if (!review.getUserId().equals(user.getId())) {
            throw new RuntimeException("리뷰를 수정할 권한이 없습니다."); // 나중에 AccessDeniedException으로 변경
        }

        review.setContent(request.getContent());
        review.setRatings(request.getRatings());
        review.setImageUrl(request.getImageUrl());
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(String reviewId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        // 리뷰 작성자도 아니고, 관리자도 아닐 경우에만 에러 발생
        if (!review.getUserId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
            throw new RuntimeException("리뷰를 삭제할 권한이 없습니다.");
        }

        // (추가) 리뷰 삭제 시 관련 '좋아요' 데이터도 함께 삭제
        likeRepository.deleteByReviewId(reviewId); // LikeRepository에 이 메서드 추가 필요
        reviewRepository.delete(review);
    }
}