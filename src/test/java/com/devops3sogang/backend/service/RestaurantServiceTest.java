package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.GeoJsonPoint;
import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.RestaurantStats;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RestaurantServiceTest {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private LikeRepository likeRepository;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 데이터 초기화
        restaurantRepository.deleteAll();
        reviewRepository.deleteAll();
        likeRepository.deleteAll();
    }

    @Test
    @DisplayName("평점순 정렬 - 전체 조회")
    void sortByRating_All() {
        // Given
        Restaurant r1 = createRestaurant("식당1", 4.5, "ON_CAMPUS", "한식", 37.5515, 126.9410);
        Restaurant r2 = createRestaurant("식당2", 4.8, "ON_CAMPUS", "일식", 37.5516, 126.9411);
        Restaurant r3 = createRestaurant("식당3", 4.2, "OFF_CAMPUS", "중식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(r1, r2, r3));

        // When
        List<Restaurant> results = restaurantService.findRestaurants(
            null, null, null, null, null, "rating");

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getStats().getRating()).isEqualTo(4.8);
        assertThat(results.get(1).getStats().getRating()).isEqualTo(4.5);
        assertThat(results.get(2).getStats().getRating()).isEqualTo(4.2);
    }

    @Test
    @DisplayName("평점순 정렬 - 카테고리 필터")
    void sortByRating_WithCategory() {
        // Given
        Restaurant r1 = createRestaurant("한식1", 4.5, "ON_CAMPUS", "한식", 37.5515, 126.9410);
        Restaurant r2 = createRestaurant("한식2", 4.8, "ON_CAMPUS", "한식", 37.5516, 126.9411);
        Restaurant r3 = createRestaurant("일식1", 4.9, "OFF_CAMPUS", "일식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(r1, r2, r3));

        // When
        List<Restaurant> results = restaurantService.findRestaurants(
            null, "한식", null, null, null, "rating");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("한식2");
        assertThat(results.get(1).getName()).isEqualTo("한식1");
    }

    @Test
    @DisplayName("거리순 정렬 테스트")
    void sortByDistance() {
        // Given: 서강대 정문 기준
        double baseLat = 37.5502;
        double baseLng = 126.9410;

        Restaurant r1 = createRestaurant("가까운", 4.0, "ON_CAMPUS", "한식", 37.5506, 126.9410);
        Restaurant r2 = createRestaurant("중간", 4.5, "ON_CAMPUS", "일식", 37.5511, 126.9410);
        Restaurant r3 = createRestaurant("먼", 4.8, "OFF_CAMPUS", "중식", 37.5520, 126.9410);
        restaurantRepository.saveAll(List.of(r1, r2, r3));

        // When
        List<Restaurant> results = restaurantService.findRestaurants(
            null, null, baseLat, baseLng, 1000, "distance");

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getName()).isEqualTo("가까운");
        assertThat(results.get(1).getName()).isEqualTo("중간");
        assertThat(results.get(2).getName()).isEqualTo("먼");
    }

    @Test
    @DisplayName("거리 검색 + 평점순 정렬")
    void distanceSearch_SortByRating() {
        // Given
        double baseLat = 37.5502;
        double baseLng = 126.9410;

        Restaurant r1 = createRestaurant("가까운저평점", 4.0, "ON_CAMPUS", "한식", 37.5506, 126.9410);
        Restaurant r2 = createRestaurant("중간고평점", 4.8, "ON_CAMPUS", "일식", 37.5511, 126.9410);
        Restaurant r3 = createRestaurant("먼고평점", 4.9, "OFF_CAMPUS", "중식", 37.5520, 126.9410);
        restaurantRepository.saveAll(List.of(r1, r2, r3));

        // When
        List<Restaurant> results = restaurantService.findRestaurants(
            null, null, baseLat, baseLng, 1000, "rating");

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getStats().getRating()).isEqualTo(4.9);
        assertThat(results.get(1).getStats().getRating()).isEqualTo(4.8);
        assertThat(results.get(2).getStats().getRating()).isEqualTo(4.0);
    }

    // Helper
    private Restaurant createRestaurant(String name, double rating, String type,
                                       String category, double lat, double lng) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setType(type);
        restaurant.setCategory(category);
        restaurant.setAddress("테스트 주소");
        restaurant.setActive(true);

        GeoJsonPoint location = new GeoJsonPoint();
        location.setCoordinates(new double[]{lng, lat}); // [경도, 위도]
        restaurant.setLocation(location);

        RestaurantStats stats = new RestaurantStats();
        stats.setRating(rating);
        stats.setReviewCount(10);
        stats.setLikeCount(5);
        restaurant.setStats(stats);

        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());

        return restaurant;
    }
}