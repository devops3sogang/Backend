package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.RestaurantStats;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.RestaurantDetailResponse;
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
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;

    @Override
    public List<Restaurant> findRestaurants(String type, String category, Double latitude, Double longitude, Integer radius, String sortBy) {
        log.info("식당 목록 조회 시작 - type: {}, category: {}, lat: {}, lng: {}, radius: {}, sortBy: {}", type, category, latitude, longitude, radius, sortBy);

        List<Restaurant> restaurants;

        // 거리 기반 검색인 경우
        if (latitude != null && longitude != null) {
            log.debug("거리 기반 조회 실행");
            double maxDistance = (radius != null) ? radius.doubleValue() : 1000.0; // 기본값 1km
            restaurants = restaurantRepository.findByDistance(latitude, longitude, maxDistance, type, category);
            log.info("거리 기반 식당 조회 완료 - 반경 {}m, 결과: {} 개", maxDistance, restaurants.size());
        
            // 거리 기반 검색 시 평점순 정렬이 요청되면 메모리에서 정렬
            if ("rating".equalsIgnoreCase(sortBy)) {
                log.debug("평점순 정렬 적용");
                restaurants = restaurants.stream()
                    .sorted((r1, r2) -> {
                        double rating1 = r1.getStats() != null ? r1.getStats().getRating() : 0.0;
                        double rating2 = r2.getStats() != null ? r2.getStats().getRating() : 0.0;
                        return Double.compare(rating2, rating1); // 내림차순
                    })
                    .toList();
            }
        } else {
            if ("rating".equalsIgnoreCase(sortBy)) {
                log.debug("평점순 정렬 조회");
                if (StringUtils.hasText(type) && StringUtils.hasText(category)) {
                    restaurants = restaurantRepository.findByTypeAndCategoryAndIsActiveTrueOrderByStats_RatingDesc(type, category);
                } else if (StringUtils.hasText(type)) {
                    restaurants = restaurantRepository.findByTypeAndIsActiveTrueOrderByStats_RatingDesc(type);
                } else if (StringUtils.hasText(category)) {
                    restaurants = restaurantRepository.findByCategoryAndIsActiveTrueOrderByStats_RatingDesc(category);
                } else {
                    restaurants = restaurantRepository.findByIsActiveTrueOrderByStats_RatingDesc();
                }
            } else {
                log.debug("기본 정렬로 조회");
                if (StringUtils.hasText(type) && StringUtils.hasText(category)) {
                    restaurants = restaurantRepository.findByTypeAndCategoryAndIsActiveTrue(type, category);
                } else if (StringUtils.hasText(type)) {
                    restaurants = restaurantRepository.findByTypeAndIsActiveTrue(type);
                } else if (StringUtils.hasText(category)) {
                    restaurants = restaurantRepository.findByCategoryAndIsActiveTrue(category);
                } else {
                    restaurants = restaurantRepository.findByIsActiveTrue();
                }
            }
            log.info("식당 조회 완료 - 결과: {} 개", restaurants.size());
        }
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
    public RestaurantDetailResponse findRestaurantDetailById(String id, String currentUserId) {
        log.info("식당 상세 조회 (리뷰 포함) 시작 - ID: {}, currentUserId: {}", id, currentUserId);

        // 1. 식당 정보 조회
        Restaurant restaurant = findRestaurantById(id);

        // 2. 식당의 모든 리뷰 조회
        List<Review> reviews = reviewRepository.findByTarget_RestaurantId(id);
        log.info("조회된 리뷰 수: {}", reviews.size());

        // 3. 리뷰를 DTO로 변환
        List<RestaurantDetailResponse.ReviewInfo> reviewInfos = reviews.stream()
                .map(review -> {
                    // 현재 사용자가 이 리뷰에 좋아요를 눌렀는지 확인
                    boolean isLiked = false;
                    if (currentUserId != null) {
                        isLiked = likeRepository.findByUserIdAndReviewId(currentUserId, review.getId()).isPresent();
                    }

                    // menuRatings 변환
                    List<RestaurantDetailResponse.MenuRatingInfo> menuRatingInfos = new ArrayList<>();
                    if (review.getRatings() != null && review.getRatings().getMenuRatings() != null) {
                        menuRatingInfos = review.getRatings().getMenuRatings().stream()
                                .map(mr -> RestaurantDetailResponse.MenuRatingInfo.builder()
                                        .menuName(mr.getMenuName())
                                        .rating(mr.getRating())
                                        .build())
                                .toList();
                    }

                    // ratings 변환
                    RestaurantDetailResponse.RatingsInfo ratingsInfo = RestaurantDetailResponse.RatingsInfo.builder()
                            .menuRatings(menuRatingInfos)
                            .restaurantRating(
                                    review.getRatings() != null ? review.getRatings().getRestaurantRating() : 0)
                            .build();

                    return RestaurantDetailResponse.ReviewInfo.builder()
                            .id(review.getId())
                            .userId(review.getUserId())
                            .nickname(review.getNickname())
                            .ratings(ratingsInfo)
                            .content(review.getContent())
                            .imageUrls(review.getImageUrls())
                            .likeCount(review.getLikeCount())
                            .createdAt(review.getCreatedAt())
                            .isLikedByCurrentUser(isLiked)
                            .build();
                })
                .toList();

        // 4. RestaurantDetailResponse 생성
        RestaurantDetailResponse response = RestaurantDetailResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .type(restaurant.getType())
                .category(restaurant.getCategory())
                .address(restaurant.getAddress())
                .location(toDto(restaurant.getLocation()))
                .imageUrl(restaurant.getImageUrl())
                .isActive(restaurant.isActive())
                .stats(restaurant.getStats())
                .menu(restaurant.getMenu())
                .reviews(reviewInfos)
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();

        log.info("식당 상세 조회 (리뷰 포함) 완료 - name: {}, 리뷰 수: {}", restaurant.getName(), reviewInfos.size());
        return response;
    }

    private com.devops3sogang.backend.dto.GeoJsonPointDTO toDto(GeoJsonPoint p) {
        if (p == null) return null;
        com.devops3sogang.backend.dto.GeoJsonPointDTO dto = new com.devops3sogang.backend.dto.GeoJsonPointDTO();
        dto.setCoordinates(new double[]{p.getX(), p.getY()}); // x=lng, y=lat
        return dto;
    }

    @Override
    public Restaurant create(RestaurantRequest request) {
        log.info("식당 등록 시작 - name: {}", request.getName());

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setType(request.getType());
        restaurant.setCategory(request.getCategory());
        restaurant.setAddress(request.getAddress());
        restaurant.setLocation(toGeoJsonPoint(request.getLocation()));
        restaurant.setMenu(request.getMenu());
        restaurant.setActive(true); // 기본값은 활성 상태

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

    private GeoJsonPoint toGeoJsonPoint(com.devops3sogang.backend.dto.GeoJsonPointDTO dto) {
        if (dto == null || dto.getCoordinates() == null || dto.getCoordinates().length != 2) {
            return null;
        }
        double lng = dto.getCoordinates()[0]; // 경도
        double lat = dto.getCoordinates()[1]; // 위도
        return new GeoJsonPoint(lng, lat);    // (lng, lat) 순서 중요!
    }

    @Override
    public Restaurant update(String restaurantId, RestaurantRequest request) {
        log.info("식당 정보 수정 시작 - restaurantId: {}", restaurantId);

        // 1. 식당이 존재하는지 확인
        Restaurant restaurant = findRestaurantById(restaurantId);

        // 2. 정보 업데이트
        restaurant.setName(request.getName());
        restaurant.setType(request.getType());
        restaurant.setCategory(request.getCategory());
        restaurant.setAddress(request.getAddress());
        restaurant.setLocation(toGeoJsonPoint(request.getLocation()));
        restaurant.setMenu(request.getMenu());

        if (request.getImageUrl() != null) {
            restaurant.setImageUrl(request.getImageUrl());
        }

        // 3. 저장
        Restaurant updated = restaurantRepository.save(restaurant);
        log.info("식당 정보 수정 완료 - restaurantId: {}, name: {}", updated.getId(), updated.getName());

        return updated;
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
                    .toList();

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

    @Override
    public void updateRestaurantStats(String restaurantId) {
        // 1. 해당 가게의 모든 리뷰를 DB에서 가져옵니다.
        List<Review> reviews = reviewRepository.findByTarget_RestaurantId(restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));

        RestaurantStats stats = restaurant.getStats();
        if (stats == null) {
            stats = new RestaurantStats(); // stats 객체가 없는 경우를 대비
        }

        if (reviews.isEmpty()) {
            // 2-1. 리뷰가 없으면 통계를 0으로 초기화합니다.
            stats.setRating(0.0);
            stats.setReviewCount(0);
        } else {
            // 2-2. 리뷰가 있으면 평균 평점을 계산합니다.
            double averageRating = reviews.stream()
                    .mapToDouble(review -> review.getRatings().getRestaurantRating())
                    .average()
                    .orElse(0.0);

            // 3. 계산된 통계를 Restaurant 객체에 업데이트합니다.
            stats.setRating(Math.round(averageRating * 10) / 10.0); // 소수점 한 자리까지
            stats.setReviewCount(reviews.size());
        }

        restaurant.setStats(stats);
        // 4. 변경된 Restaurant 정보를 DB에 다시 저장합니다.
        restaurantRepository.save(restaurant);
    }
}