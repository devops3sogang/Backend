package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

//import java.util.List;

public interface RestaurantRepository extends MongoRepository<Restaurant, String>, RestaurantCustomRepository {
    boolean existsByNameAndAddress(String name, String address);

// 리팩터링 후 삭제 
//    List<Restaurant> findByIsActiveTrue();
//    List<Restaurant> findByTypeAndIsActiveTrue(String type);
//    List<Restaurant> findByCategoryAndIsActiveTrue(String category);
//    List<Restaurant> findByTypeAndCategoryAndIsActiveTrue(String type, String category);
}