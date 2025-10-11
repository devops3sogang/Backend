package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.RestaurantStats;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.RestaurantRequest;
import com.devops3sogang.backend.exception.RestaurantNotFoundException;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j  // Lombok의 SLF4J Logger 생성
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;

    @Override
    public List<Restaurant> findRestaurants(String type, String category) {
        log.info("식당 목록 조회 시작 - type: {}, category: {}", type, category);
        
        List<Restaurant> restaurants;
        
        if (StringUtils.hasText(type) && StringUtils.hasText(category)) {
            log.debug("필터 적용: type과 category 모두");
            restaurants = restaurantRepository.findByTypeAndCategoryAndIsActiveTrue(type, category);
        } else if (StringUtils.hasText(type)) {
            log.debug("필터 적용: type만");
            restaurants = restaurantRepository.findByTypeAndIsActiveTrue(type);
        } else if (StringUtils.hasText(category)) {
            log.debug("필터 적용: category만");
            restaurants = restaurantRepository.findByCategoryAndIsActiveTrue(category);
        } else {
            log.debug("필터 적용: 없음 (전체 조회)");
            restaurants = restaurantRepository.findByIsActiveTrue();
        }
        
        log.info("식당 목록 조회 완료 - 결과: {} 개", restaurants.size());
        return restaurants;
    }

    @Override
    public Restaurant findRestaurantById(String id) {
        log.info("식당 상세 조회 시작 - ID: {}", id);
        
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("식당을 찾을 수 없음 - ID: {}", id);
                    return new RestaurantNotFoundException(id);
                });
        
        log.info("식당 상세 조회 완료 - name: {}", restaurant.getName());
        return restaurant;
    }

    @Override
    public Restaurant create(RestaurantRequest request) {
        log.info("식당 등록 시작 - name: {}", request.getName());
        
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setType(request.getType());
        restaurant.setCategory(request.getCategory());
        restaurant.setAddress(request.getAddress());
        restaurant.setLocation(request.getLocation());
        restaurant.setMenu(request.getMenu());
        restaurant.setActive(true);  // 기본값은 활성 상태
        
        // Stats 초기화
        RestaurantStats stats = new RestaurantStats();
        stats.setRating(0.0);
        stats.setReviewCount(0);
        stats.setLikeCount(0);
        restaurant.setStats(stats);
        log.debug("RestaurantStats 초기화 완료");
        
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("식당 등록 완료 - ID: {}, name: {}", saved.getId(), saved.getName());
        
        return saved;
    }

    @Override
    @Transactional
    public void deleteRestaurant(String restaurantId) {
        log.info("식당 삭제 시작 - ID: {}", restaurantId);
        
        // 1. findRestaurantById()로 존재 확인
        // 없으면 RestaurantNotFoundException 던짐
        Restaurant restaurant = findRestaurantById(restaurantId);
        log.debug("삭제 대상 식당 확인됨 - name: {}", restaurant.getName());
        
        // 2. 삭제할 맛집에 달린 모든 리뷰를 조회
        List<Review> reviewsToDelete = reviewRepository.findByTarget_RestaurantId(restaurantId);
        log.info("삭제할 리뷰 수: {}", reviewsToDelete.size());
        
        if (!reviewsToDelete.isEmpty()) {
            // 3. 리뷰들의 ID 목록을 추출
            List<String> reviewIdsToDelete = reviewsToDelete.stream()
                    .map(Review::getId)
                    .collect(Collectors.toList());
            
            log.debug("리뷰 ID 목록 추출 완료 - {}개", reviewIdsToDelete.size());
            
            // 4. 해당 리뷰들에 달린 모든 '좋아요'를 삭제
            likeRepository.deleteAllByReviewIdIn(reviewIdsToDelete);
            log.debug("리뷰의 좋아요 삭제 완료");
            
            // 5. 모든 리뷰를 삭제
            reviewRepository.deleteAll(reviewsToDelete);
            log.debug("리뷰 삭제 완료");
        }
        
        // 6. 마지막으로 맛집을 삭제
        restaurantRepository.deleteById(restaurantId);
        log.info("식당 삭제 완료 - ID: {}", restaurantId);
    }
}