package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.dto.RestaurantRequest;
import com.devops3sogang.backend.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
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
}