package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByTarget_RestaurantId(String restaurantId);
    List<Review> findByUserId(String userId);
    void deleteByTarget_RestaurantId(String restaurantId);
    
    // 최신 리뷰 조회 (생성일 기준 내림차순)
    List<Review> findTop5ByOrderByCreatedAtDesc();  // 상위 5개
    List<Review> findAllByOrderByCreatedAtDesc();   // 전체 (최신순)
}