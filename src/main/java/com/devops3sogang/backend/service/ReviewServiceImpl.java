package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Type;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.ReviewTarget;
import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.document.MenuItem;
import com.devops3sogang.backend.dto.ReviewRequest;
import com.devops3sogang.backend.dto.ReviewUpdateRequest;
import com.devops3sogang.backend.dto.ReviewResponse;

import com.devops3sogang.backend.exception.*;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import com.devops3sogang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
    public Review createReview(String userEmail, ReviewRequest request) {
        log.info("리뷰 작성 시작 - userEmail: {}, restaurantId: {}", userEmail, request.getRestaurantId());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        var restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(request.getRestaurantId()));

        Review review = new Review();
        review.setUserId(user.getId());
        review.setNickname(user.getNickname());
        review.setContent(request.getContent());
        review.setRating(request.getRating());
        review.setImageUrls(request.getImageUrls());
        review.setLikeCount(0);

        ReviewTarget target = new ReviewTarget();
        target.setType(request.getTargetType());
        target.setRestaurantId(request.getRestaurantId());

        if (request.getTargetType() == Type.MENU) {
            target.setMenuIds(request.getMenuIds());

            // 메뉴 ID validation (Restaurant의 menu 필드에 있는 경우만)
            // OnCampusMenu의 메뉴는 별도 document에 있으므로 validation skip
            List<MenuItem> menuList = restaurant.getMenu();
            if (menuList != null && !menuList.isEmpty()) {
                request.getMenuIds().forEach(menuId -> {
                    boolean exists = menuList.stream()
                            .anyMatch(m -> m.getId().equals(menuId));
                    if (!exists) {
                        throw new MenuNotFoundInRestaurantException(menuId);
                    }
                });
            }
            // restaurant.getMenu()이 null이거나 비어있으면 validation skip
            // (OnCampusMenu 등 별도 document에 메뉴가 있는 경우)
        }

        review.setTarget(target);

        Review saved = reviewRepository.save(review);

        restaurantService.updateRestaurantStats(request.getRestaurantId());

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
    public List<Review> findReviewsByMenu(String restaurantId, String menuId) {
        if (restaurantId == null || menuId == null) {
            throw new IllegalArgumentException("restaurantId와 menuId는 필수입니다.");
        }

        // 1) target.menuId 기반 조회
        List<Review> directMatches = reviewRepository.findByTarget_RestaurantIdAndTarget_MenuIdsContains(restaurantId,
                menuId);

        // 2) rating.menuRatings 기반 조회
        List<Review> ratingMatches = reviewRepository
                .findByTarget_RestaurantIdAndRating_MenuRatings_MenuId(restaurantId, menuId);

        // 중복 제거 후 반환 (두 기준에서 동시에 걸릴 수도 있음)
        Set<Review> merged = new LinkedHashSet<>();
        merged.addAll(directMatches);
        merged.addAll(ratingMatches);

        return new ArrayList<>(merged);
    }

    @Override
    public List<Review> findRecentReviews(int limit) {
        log.info("최신 리뷰 조회 시작 - limit: {}", limit);

        List<Review> reviews = reviewRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();

        log.info("최신 리뷰 조회 완료 - 결과: {} 개", reviews.size());
        return reviews;
    }

    @Override
    public Review updateReview(String reviewId, ReviewUpdateRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("리뷰를 수정할 권한이 없습니다.");
        }

        Type targetType = review.getTarget().getType();

        List<String> menuIds = request.getMenuIds();
        if (targetType == Type.MENU) {
            if (menuIds == null || menuIds.size() != 1) {
                throw new IllegalArgumentException("메뉴 리뷰는 menuIds가 정확히 1개여야 합니다.");
            }
        }

        if (targetType == Type.RESTAURANT && menuIds != null) {
            int ratingCount = request.getRating().getMenuRatings() == null
                    ? 0
                    : request.getRating().getMenuRatings().size();
            if (ratingCount != menuIds.size()) {
                throw new IllegalArgumentException(
                        "선택한 메뉴 수와 입력한 메뉴 평점 수가 일치해야 합니다.");
            }
        }

        if (menuIds != null) {
            review.getTarget().setMenuIds(menuIds);
        }

        review.setContent(request.getContent());
        review.setRating(request.getRating());
        review.setImageUrls(request.getImageUrls());

        Review updated = reviewRepository.save(review);

        restaurantService.updateRestaurantStats(review.getTarget().getRestaurantId());

        return updated;
    }

    @Override
    public void deleteReview(String reviewId, String userEmail) {
        log.info("리뷰 삭제 시작 - reviewId: {}, userEmail: {}", reviewId, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.getUserId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
        }

        likeRepository.deleteByReviewId(reviewId);
        log.debug("리뷰 좋아요 삭제 완료 - reviewId: {}", reviewId);

        String restaurantId = review.getTarget().getRestaurantId();
        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료 - reviewId: {}, restaurantId: {}", reviewId, restaurantId);

        try {
            restaurantService.updateRestaurantStats(restaurantId);
        } catch (Exception e) {
            log.error("식당 통계 갱신 중 오류 발생 - restaurantId: {}", restaurantId, e);
        }
    }
}