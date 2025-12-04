package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByTarget_RestaurantId(String restaurantId);
    List<Review> findByUserId(String userId);
    void deleteByTarget_RestaurantId(String restaurantId);
    List<Review> findByTarget_RestaurantIdAndTarget_MenuIdsContains(String restaurantId, String menuId);
    List<Review> findByTarget_RestaurantIdAndRating_MenuRatings_MenuId(String restaurantId, String menuId);
}