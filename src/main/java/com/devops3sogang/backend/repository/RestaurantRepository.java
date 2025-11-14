package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RestaurantRepository extends MongoRepository<Restaurant, String>, RestaurantCustomRepository {
    boolean existsByNameAndAddress(String name, String address);
    List<Restaurant> findByIsActiveTrue();
    List<Restaurant> findByTypeAndIsActiveTrue(String type);
    List<Restaurant> findByCategoryAndIsActiveTrue(String category);
    List<Restaurant> findByTypeAndCategoryAndIsActiveTrue(String type, String category);
    
    //정렬을 enum으로 사용하면서 다음과같은 쿼리메소드는 불필요. 리팩터링 완료 후 삭제 예정. 
    //List<Restaurant> findByIsActiveTrueOrderByStats_RatingDesc();
    //List<Restaurant> findByTypeAndIsActiveTrueOrderByStats_RatingDesc(String type);
    //List<Restaurant> findByCategoryAndIsActiveTrueOrderByStats_RatingDesc(String category);
    //List<Restaurant> findByTypeAndCategoryAndIsActiveTrueOrderByStats_RatingDesc(String type, String category);
}