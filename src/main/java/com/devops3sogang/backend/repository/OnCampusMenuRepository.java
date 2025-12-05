package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.OnCampusMenu;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OnCampusMenuRepository extends MongoRepository<OnCampusMenu, String> {
    @Query("{ 'restaurantId': ?0, 'weekStartDate': ?1 }")
    List<OnCampusMenu> findByRestaurantIdAndWeekStartDate(String restaurantId, String weekStartDate);

    @Query("{ 'restaurantId': ?0, 'dailyMenus.date': ?1 }")
    List<OnCampusMenu> findByRestaurantIdAndDailyMenusDate(String restaurantId, String date);
}