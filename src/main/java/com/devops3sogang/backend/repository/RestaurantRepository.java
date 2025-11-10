package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends MongoRepository<Restaurant, String>, RestaurantCustomRepository {
    Optional<Restaurant> findByNameAndAddress(String name, String address);

    List<Restaurant> findByTypeAndCategoryAndIsActiveTrue(String type, String category);
    List<Restaurant> findByTypeAndIsActiveTrue(String type);
    List<Restaurant> findByCategoryAndIsActiveTrue(String category);
    List<Restaurant> findByIsActiveTrue();
    // 평점순 정렬 (전체)
    List<Restaurant> findByIsActiveTrueOrderByStats_RatingDesc();
    // 평점순 정렬 (type 필터)
    List<Restaurant> findByTypeAndIsActiveTrueOrderByStats_RatingDesc(String type);
    // 평점순 정렬 (category 필터)
    List<Restaurant> findByCategoryAndIsActiveTrueOrderByStats_RatingDesc(String category);
    // 평점순 정렬 (type + category 필터)
    List<Restaurant> findByTypeAndCategoryAndIsActiveTrueOrderByStats_RatingDesc(String type, String category);
}