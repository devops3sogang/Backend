package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.dto.RestaurantSearchRequest;

import java.util.List;

public interface RestaurantCustomRepository {
    List<Restaurant> search(RestaurantSearchRequest request);
}