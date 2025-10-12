package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.dto.RestaurantDetailResponse;
import com.devops3sogang.backend.dto.RestaurantRequest;

import java.util.List;

public interface RestaurantService {
    List<Restaurant> findRestaurants(String type, String category);
    Restaurant findRestaurantById(String id);
    RestaurantDetailResponse findRestaurantDetailById(String id, String currentUserId);
    Restaurant create(RestaurantRequest request);
    Restaurant update(String restaurantId, RestaurantRequest request);
    void deleteRestaurant(String restaurantId);
}
