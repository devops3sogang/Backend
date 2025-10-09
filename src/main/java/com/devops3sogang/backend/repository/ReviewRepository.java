package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    // 맛집 ID를 통해 해당 맛집의 모든 리뷰를 찾는 쿼리 메서드
    // Document 내부 객체의 필드로 검색: target 객체의 restaurantId 필드
    List<Review> findByTarget_RestaurantId(String restaurantId);
}