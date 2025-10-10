package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.dto.RestaurantRequest;

import java.util.List;

public interface RestaurantService {
    List<Restaurant> findRestaurants(String type, String category);
    Restaurant findRestaurantById(String id);
    Restaurant create(RestaurantRequest request);
    void deleteRestaurant(String restaurantId);
}
