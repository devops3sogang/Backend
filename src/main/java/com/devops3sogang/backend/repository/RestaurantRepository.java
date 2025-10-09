package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
    // 필요한 경우 커스텀 쿼리 메서드를 추가할 수 있습니다.
    // 예: List<Restaurant> findByCategory(String category);
}