package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Restaurant;
import java.util.List;

public interface RestaurantCustomRepository {
    List<Restaurant> findByDistance(double latitude, double longitude, double maxDistanceInMeters, String type, String category);
}