/*package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.RestaurantStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
    }

    @Test
    @DisplayName("활성화된 식당만 조회")
    void findByIsActiveTrue() {
        // Given
        Restaurant active = createRestaurant("활성식당", 4.5, "ON_CAMPUS", "한식", true);
        Restaurant inactive = createRestaurant("비활성식당", 4.0, "ON_CAMPUS", "한식", false);
        restaurantRepository.saveAll(List.of(active, inactive));

        // When
        List<Restaurant> results = restaurantRepository.findByIsActiveTrue();

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("활성식당");
    }

    @Test
    @DisplayName("타입 필터링 조회")
    void findByTypeAndIsActiveTrue() {
        // Given
        Restaurant onCampus1 = createRestaurant("교내1", 4.5, "ON_CAMPUS", "한식", true);
        Restaurant onCampus2 = createRestaurant("교내2", 4.3, "ON_CAMPUS", "일식", true);
        Restaurant offCampus = createRestaurant("교외", 4.8, "OFF_CAMPUS", "중식", true);
        restaurantRepository.saveAll(List.of(onCampus1, onCampus2, offCampus));

        // When
        List<Restaurant> results = restaurantRepository.findByTypeAndIsActiveTrue("ON_CAMPUS");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Restaurant::getType).containsOnly("ON_CAMPUS");
    }

    @Test
    @DisplayName("카테고리 필터링 조회")
    void findByCategoryAndIsActiveTrue() {
        // Given
        Restaurant korean1 = createRestaurant("한식1", 4.5, "ON_CAMPUS", "한식", true);
        Restaurant korean2 = createRestaurant("한식2", 4.3, "ON_CAMPUS", "한식", true);
        Restaurant japanese = createRestaurant("일식", 4.8, "OFF_CAMPUS", "일식", true);
        restaurantRepository.saveAll(List.of(korean1, korean2, japanese));

        // When
        List<Restaurant> results = restaurantRepository.findByCategoryAndIsActiveTrue("한식");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Restaurant::getCategory).containsOnly("한식");
    }

    @Test
    @DisplayName("타입과 카테고리 필터링 조회")
    void findByTypeAndCategoryAndIsActiveTrue() {
        // Given
        Restaurant target = createRestaurant("목표", 4.5, "ON_CAMPUS", "한식", true);
        Restaurant wrongType = createRestaurant("타입다름", 4.3, "OFF_CAMPUS", "한식", true);
        Restaurant wrongCategory = createRestaurant("카테고리다름", 4.8, "ON_CAMPUS", "일식", true);
        restaurantRepository.saveAll(List.of(target, wrongType, wrongCategory));

        // When
        List<Restaurant> results = restaurantRepository
                .findByTypeAndCategoryAndIsActiveTrue("ON_CAMPUS", "한식");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("목표");
    }

    @Test
    @DisplayName("평점순 정렬 - 전체")
    void findByIsActiveTrueOrderByStats_RatingDesc() {
        // Given
        Restaurant low = createRestaurant("낮은평점", 4.0, "ON_CAMPUS", "한식", true);
        Restaurant high = createRestaurant("높은평점", 4.8, "ON_CAMPUS", "일식", true);
        Restaurant medium = createRestaurant("중간평점", 4.5, "OFF_CAMPUS", "중식", true);
        restaurantRepository.saveAll(List.of(low, high, medium));

        // When
        List<Restaurant> results = restaurantRepository.findByIsActiveTrueOrderByStats_RatingDesc();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getStats().getRating()).isEqualTo(4.8);
        assertThat(results.get(1).getStats().getRating()).isEqualTo(4.5);
        assertThat(results.get(2).getStats().getRating()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("평점순 정렬 - 타입 필터")
    void findByTypeAndIsActiveTrueOrderByStats_RatingDesc() {
        // Given
        Restaurant onCampus1 = createRestaurant("교내1", 4.2, "ON_CAMPUS", "한식", true);
        Restaurant onCampus2 = createRestaurant("교내2", 4.8, "ON_CAMPUS", "일식", true);
        Restaurant offCampus = createRestaurant("교외", 4.9, "OFF_CAMPUS", "중식", true);
        restaurantRepository.saveAll(List.of(onCampus1, onCampus2, offCampus));

        // When
        List<Restaurant> results = restaurantRepository
                .findByTypeAndIsActiveTrueOrderByStats_RatingDesc("ON_CAMPUS");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStats().getRating()).isEqualTo(4.8);
        assertThat(results.get(1).getStats().getRating()).isEqualTo(4.2);
    }

    @Test
    @DisplayName("평점순 정렬 - 카테고리 필터")
    void findByCategoryAndIsActiveTrueOrderByStats_RatingDesc() {
        // Given
        Restaurant korean1 = createRestaurant("한식1", 4.3, "ON_CAMPUS", "한식", true);
        Restaurant korean2 = createRestaurant("한식2", 4.7, "OFF_CAMPUS", "한식", true);
        Restaurant japanese = createRestaurant("일식", 4.9, "ON_CAMPUS", "일식", true);
        restaurantRepository.saveAll(List.of(korean1, korean2, japanese));

        // When
        List<Restaurant> results = restaurantRepository
                .findByCategoryAndIsActiveTrueOrderByStats_RatingDesc("한식");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStats().getRating()).isEqualTo(4.7);
        assertThat(results.get(1).getStats().getRating()).isEqualTo(4.3);
    }

    @Test
    @DisplayName("평점순 정렬 - 타입과 카테고리 필터")
    void findByTypeAndCategoryAndIsActiveTrueOrderByStats_RatingDesc() {
        // Given
        Restaurant target1 = createRestaurant("목표1", 4.5, "ON_CAMPUS", "한식", true);
        Restaurant target2 = createRestaurant("목표2", 4.8, "ON_CAMPUS", "한식", true);
        Restaurant wrongType = createRestaurant("타입다름", 4.9, "OFF_CAMPUS", "한식", true);
        Restaurant wrongCategory = createRestaurant("카테고리다름", 4.7, "ON_CAMPUS", "일식", true);
        restaurantRepository.saveAll(List.of(target1, target2, wrongType, wrongCategory));

        // When
        List<Restaurant> results = restaurantRepository
                .findByTypeAndCategoryAndIsActiveTrueOrderByStats_RatingDesc("ON_CAMPUS", "한식");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStats().getRating()).isEqualTo(4.8);
        assertThat(results.get(1).getStats().getRating()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("stats가 null인 식당 처리")
    void handleNullStats() {
        // Given
        Restaurant withStats = createRestaurant("통계있음", 4.5, "ON_CAMPUS", "한식", true);
        Restaurant withoutStats = createRestaurant("통계없음", 0.0, "ON_CAMPUS", "한식", true);
        withoutStats.setStats(null);
        restaurantRepository.saveAll(List.of(withStats, withoutStats));

        // When
        List<Restaurant> results = restaurantRepository
                .findByIsActiveTrueOrderByStats_RatingDesc();

        // Then
        assertThat(results).hasSize(2);
        // null stats는 마지막에 정렬되어야 함
        assertThat(results.get(0).getStats()).isNotNull();
    }

    // Helper
    private Restaurant createRestaurant(String name, double rating, String type,
                                        String category, boolean isActive) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setType(type);
        restaurant.setCategory(category);
        restaurant.setAddress("테스트 주소");
        restaurant.setActive(isActive);

        restaurant.setLocation(new GeoJsonPoint(126.9410, 37.5515));

        RestaurantStats stats = new RestaurantStats();
        stats.setRating(rating);
        stats.setReviewCount(10);
        stats.setLikeCount(5);
        restaurant.setStats(stats);

        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());

        return restaurant;
    }
}*/