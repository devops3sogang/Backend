package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.ReviewTarget;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.dto.ReviewUpdateRequest;
import com.devops3sogang.backend.exception.AccessDeniedException;
import com.devops3sogang.backend.exception.RestaurantNotFoundException;
import com.devops3sogang.backend.exception.ReviewNotFoundException;
import com.devops3sogang.backend.exception.UserNotFoundException;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import com.devops3sogang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final LikeRepository likeRepository;
    private final RestaurantService restaurantService;

    @Override
    public Review createReview(String userEmail, String restaurantId, ReviewRequest request) {
        log.info("리뷰 작성 시작 - userEmail: {}, restaurantId: {}", userEmail, restaurantId);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - email: {}", userEmail);
                    return new UserNotFoundException(userEmail);
                });

        var restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    log.warn("식당을 찾을 수 없음 - id: {}", restaurantId);
                    return new RestaurantNotFoundException(restaurantId);
                });

        Review review = new Review();
        review.setUserId(user.getId());
        review.setNickname(user.getNickname());

        ReviewTarget target = new ReviewTarget();
        target.setType("RESTAURANT");
        target.setRestaurantId(restaurantId);
        target.setRestaurantName(restaurant.getName());
        review.setTarget(target);

        review.setContent(request.getContent());
        review.setRatings(request.getRatings());
        review.setImageUrls(request.getImageUrls());
        review.setLikeCount(0);
        // createdAt, updatedAt은 @CreatedDate, @LastModifiedDate로 자동 설정됨

        Review saved = reviewRepository.save(review);
        restaurantService.updateRestaurantStats(restaurantId);
        log.info("리뷰 작성 완료 - reviewId: {}", saved.getId());

        return saved;
    }

    @Override
    public List<Review> findReviewsByRestaurantId(String restaurantId) {
        log.info("식당의 리뷰 목록 조회 시작 - restaurantId: {}", restaurantId);

        List<Review> reviews = reviewRepository.findByTarget_RestaurantId(restaurantId);

        log.info("식당의 리뷰 목록 조회 완료 - 결과: {} 개", reviews.size());
        return reviews;
    }

    @Override
    public List<Review> findRecentReviews(int limit) {
        log.info("최신 리뷰 조회 시작 - limit: {}", limit);

        List<Review> reviews;

        if (limit == 5) {
            reviews = reviewRepository.findTop5ByOrderByCreatedAtDesc();
        } else {
            reviews = reviewRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .limit(limit)
                    .toList();
        }

        log.info("최신 리뷰 조회 완료 - 결과: {} 개", reviews.size());
        return reviews;
    }

    @Override
    public Review updateReview(String reviewId, ReviewUpdateRequest request, String userEmail) {
        log.info("리뷰 수정 시작 - reviewId: {}, userEmail: {}", reviewId, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - email: {}", userEmail);
                    return new UserNotFoundException(userEmail);
                });

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("리뷰를 찾을 수 없음 - id: {}", reviewId);
                    return new ReviewNotFoundException(reviewId);
                });

        if (!review.getUserId().equals(user.getId())) {
            log.warn("리뷰 수정 권한 없음 - reviewId: {}, userId: {}", reviewId, user.getId());
            throw new AccessDeniedException("리뷰를 수정할 권한이 없습니다.");
        }

        review.setContent(request.getContent());
        review.setRatings(request.getRatings());
        review.setImageUrls(request.getImageUrls());
        // updatedAt은 @LastModifiedDate로 자동 업데이트됨

        Review updated = reviewRepository.save(review);
        String restaurantId = review.getTarget().getRestaurantId();
        restaurantService.updateRestaurantStats(restaurantId);
        log.info("리뷰 수정 완료 - reviewId: {}", updated.getId());

        return updated;
    }

    @Override
    public void deleteReview(String reviewId, String userEmail) {
        log.info("리뷰 삭제 시작 - reviewId: {}, userEmail: {}", reviewId, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - email: {}", userEmail);
                    return new UserNotFoundException(userEmail);
                });

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("리뷰를 찾을 수 없음 - id: {}", reviewId);
                    return new ReviewNotFoundException(reviewId);
                });

        // 권한 확인
        if (!review.getUserId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
            log.warn("리뷰 삭제 권한 없음 - reviewId: {}, userId: {}", reviewId, user.getId());
            throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
        }

        // 1️⃣ 먼저 관련 "좋아요" 데이터 삭제
        likeRepository.deleteByReviewId(reviewId);
        log.debug("리뷰 좋아요 삭제 완료 - reviewId: {}", reviewId);

        // 2️⃣ 그 다음 리뷰 삭제
        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료 - reviewId: {}", reviewId);

        String restaurantId = review.getTarget().getRestaurantId();
        restaurantService.updateRestaurantStats(restaurantId);
    }
}