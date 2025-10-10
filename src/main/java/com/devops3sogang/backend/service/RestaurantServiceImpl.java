package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.RestaurantRequest;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Restaurant> findRestaurants(String type, String category) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        if (StringUtils.hasText(type)) {
            criteria.and("type").is(type);
        }
        if (StringUtils.hasText(category)) {
            criteria.and("category").is(category);
        }

        query.addCriteria(criteria);
        return mongoTemplate.find(query, Restaurant.class);
    }

    @Override
    public Restaurant findRestaurantById(String id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("맛집 정보를 찾을 수 없습니다. ID: " + id));
    }

    @Override
    public Restaurant create(RestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setType(request.getType());
        restaurant.setCategory(request.getCategory());
        restaurant.setAddress(request.getAddress());
        restaurant.setLocation(request.getLocation());
        restaurant.setMenu(request.getMenu());
        restaurant.setActive(true); // 기본값은 활성 상태로
        return restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional
    public void deleteRestaurant(String restaurantId) {
        // 1. 맛집이 존재하는지 확인
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RuntimeException("맛집 정보를 찾을 수 없습니다. ID: " + restaurantId);
        }

        // 2. 삭제할 맛집에 달린 모든 리뷰를 조회
        List<Review> reviewsToDelete = reviewRepository.findByTarget_RestaurantId(restaurantId);

        if (!reviewsToDelete.isEmpty()) {
            // 3. 리뷰들의 ID 목록을 추출
            List<String> reviewIdsToDelete = reviewsToDelete.stream()
                    .map(Review::getId)
                    .collect(Collectors.toList());

            // 4. 해당 리뷰들에 달린 모든 '좋아요'를 삭제
            likeRepository.deleteAllByReviewIdIn(reviewIdsToDelete);

            // 5. 모든 리뷰를 삭제
            reviewRepository.deleteAll(reviewsToDelete);
        }

        // 6. 마지막으로 맛집을 삭제
        restaurantRepository.deleteById(restaurantId);
    }
}