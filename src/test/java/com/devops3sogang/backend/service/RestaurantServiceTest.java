package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.RestaurantStats;
import com.devops3sogang.backend.document.SortBy;
import com.devops3sogang.backend.dto.RestaurantSearchRequest;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import com.devops3sogang.backend.repository.LikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private static final double BASE_LAT = 37.5500;
    private static final double BASE_LNG = 126.9400;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
        reviewRepository.deleteAll();
        likeRepository.deleteAll();
    }

    // ========== 정상 케이스 14개 ==========

    @Test
    @DisplayName("1. 필터X & 정렬기준X: 모든 식당 랜덤(DB 순서)")
    void search_NoFilter_NoSort() {
        // Given
        Restaurant r1 = createRestaurant("식당1", 4.5, 10, "한식", 37.5515, 126.9410);
        Restaurant r2 = createRestaurant("식당2", 4.8, 20, "일식", 37.5516, 126.9411);
        restaurantRepository.saveAll(List.of(r1, r2));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Restaurant::getName)
                .containsExactlyInAnyOrder("식당1", "식당2");
    }

    @Test
    @DisplayName("2. 필터X & 거리순 정렬: 모든 식당 거리 오름차순")
    void search_NoFilter_SortByDistance() {
        // Given
        Restaurant near = createRestaurant("가까운", 4.0, 10, "한식", 37.5506, 126.9400);
        Restaurant medium = createRestaurant("중간", 4.5, 15, "일식", 37.5511, 126.9400);
        Restaurant far = createRestaurant("먼", 4.9, 20, "중식", 37.5520, 126.9400);
        restaurantRepository.saveAll(List.of(far, near, medium));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setSortBy(SortBy.DISTANCE);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getName()).isEqualTo("가까운");
        assertThat(results.get(1).getName()).isEqualTo("중간");
        assertThat(results.get(2).getName()).isEqualTo("먼");
    }

    @Test
    @DisplayName("3. 필터X & 평점순 정렬: 모든 식당 평점 내림차순")
    void search_NoFilter_SortByRating() {
        // Given
        Restaurant low = createRestaurant("낮은평점", 4.0, 10, "한식", 37.5515, 126.9410);
        Restaurant high = createRestaurant("높은평점", 4.9, 15, "일식", 37.5516, 126.9411);
        Restaurant medium = createRestaurant("중간평점", 4.5, 20, "중식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(low, high, medium));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setSortBy(SortBy.RATING);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getStats().getRating()).isEqualTo(4.9);
        assertThat(results.get(1).getStats().getRating()).isEqualTo(4.5);
        assertThat(results.get(2).getStats().getRating()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("4. 필터X & 인기순 정렬: 모든 식당 리뷰 개수 내림차순")
    void search_NoFilter_SortByPopular() {
        // Given
        Restaurant r1 = createRestaurant("식당1", 4.5, 5, "한식", 37.5515, 126.9410);
        Restaurant r2 = createRestaurant("식당2", 4.7, 20, "일식", 37.5516, 126.9411);
        Restaurant r3 = createRestaurant("식당3", 4.2, 12, "중식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(r1, r2, r3));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setSortBy(SortBy.POPULAR);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getStats().getReviewCount()).isEqualTo(20);
        assertThat(results.get(1).getStats().getReviewCount()).isEqualTo(12);
        assertThat(results.get(2).getStats().getReviewCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("5. 거리필터 & 정렬기준X: 거리 범위 내 식당 거리 오름차순")
    void search_DistanceFilter_NoSort() {
        // Given
        Restaurant near = createRestaurant("가까운", 4.0, 10, "한식", 37.5506, 126.9400);
        Restaurant far = createRestaurant("먼", 4.8, 15, "일식", 37.5600, 126.9400);
        restaurantRepository.saveAll(List.of(near, far));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setRadius(500);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("가까운");
    }

    @Test
    @DisplayName("6. 거리필터 & 평점순 정렬: 거리 범위 내 식당 평점 내림차순")
    void search_DistanceFilter_SortByRating() {
        // Given
        Restaurant lowRating = createRestaurant("낮은평점", 4.0, 10, "한식", 37.5506, 126.9400);
        Restaurant highRating = createRestaurant("높은평점", 4.8, 15, "일식", 37.5511, 126.9400);
        Restaurant far = createRestaurant("먼", 4.9, 20, "중식", 37.5600, 126.9400);
        restaurantRepository.saveAll(List.of(lowRating, highRating, far));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setRadius(1000);
        request.setSortBy(SortBy.RATING);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStats().getRating()).isEqualTo(4.8);
        assertThat(results.get(1).getStats().getRating()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("7. 거리필터 & 인기순 정렬: 거리 범위 내 식당 리뷰 개수 내림차순")
    void search_DistanceFilter_SortByPopular() {
        // Given
        Restaurant lowReview = createRestaurant("리뷰적음", 4.0, 5, "한식", 37.5506, 126.9400);
        Restaurant highReview = createRestaurant("리뷰많음", 4.2, 15, "일식", 37.5507, 126.9400);
        Restaurant far = createRestaurant("먼", 4.9, 100, "중식", 37.5600, 126.9400);
        restaurantRepository.saveAll(List.of(lowReview, highReview, far));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setRadius(500);
        request.setSortBy(SortBy.POPULAR);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStats().getReviewCount()).isEqualTo(15);
        assertThat(results.get(1).getStats().getReviewCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("8. 카테고리필터 & 정렬기준X: 해당 카테고리 식당 랜덤(DB 순서)")
    void search_CategoryFilter_NoSort() {
        // Given
        Restaurant korean1 = createRestaurant("한식1", 4.5, 10, "한식", 37.5515, 126.9410);
        Restaurant korean2 = createRestaurant("한식2", 4.3, 15, "한식", 37.5516, 126.9411);
        Restaurant japanese = createRestaurant("일식", 4.8, 20, "일식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(korean1, korean2, japanese));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setCategory("한식");
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Restaurant::getCategory).containsOnly("한식");
    }

    @Test
    @DisplayName("9. 카테고리필터 & 거리순 정렬: 해당 카테고리 식당 거리 오름차순")
    void search_CategoryFilter_SortByDistance() {
        // Given
        Restaurant nearKorean = createRestaurant("가까운한식", 4.2, 10, "한식", 37.5506, 126.9400);
        Restaurant farKorean = createRestaurant("먼한식", 4.7, 15, "한식", 37.5520, 126.9400);
        Restaurant japanese = createRestaurant("일식", 4.9, 20, "일식", 37.5505, 126.9400);
        restaurantRepository.saveAll(List.of(farKorean, nearKorean, japanese));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setCategory("한식");
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setSortBy(SortBy.DISTANCE);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("가까운한식");
        assertThat(results.get(1).getName()).isEqualTo("먼한식");
        assertThat(results).extracting(Restaurant::getCategory).containsOnly("한식");
    }

    @Test
    @DisplayName("10. 카테고리필터 & 평점순 정렬: 해당 카테고리 식당 평점 내림차순")
    void search_CategoryFilter_SortByRating() {
        // Given
        Restaurant korean1 = createRestaurant("한식1", 4.3, 10, "한식", 37.5515, 126.9410);
        Restaurant korean2 = createRestaurant("한식2", 4.7, 15, "한식", 37.5516, 126.9411);
        Restaurant japanese = createRestaurant("일식", 4.9, 20, "일식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(korean1, korean2, japanese));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setCategory("한식");
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setSortBy(SortBy.RATING);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStats().getRating()).isEqualTo(4.7);
        assertThat(results.get(1).getStats().getRating()).isEqualTo(4.3);
        assertThat(results).extracting(Restaurant::getCategory).containsOnly("한식");
    }

    @Test
    @DisplayName("11. 카테고리필터 & 인기순 정렬: 해당 카테고리 식당 리뷰 개수 내림차순")
    void search_CategoryFilter_SortByPopular() {
        // Given
        Restaurant k1 = createRestaurant("한식1", 4.0, 3, "한식", 37.5515, 126.9410);
        Restaurant k2 = createRestaurant("한식2", 4.5, 10, "한식", 37.5516, 126.9411);
        Restaurant j1 = createRestaurant("일식", 4.8, 20, "일식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(k1, k2, j1));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setCategory("한식");
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setSortBy(SortBy.POPULAR);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStats().getReviewCount()).isEqualTo(10);
        assertThat(results.get(1).getStats().getReviewCount()).isEqualTo(3);
        assertThat(results).extracting(Restaurant::getCategory).containsOnly("한식");
    }

    @Test
    @DisplayName("12. 거리+카테고리필터 & 정렬기준X: 거리 범위 내 해당 카테고리 식당 거리 오름차순")
    void search_DistanceAndCategoryFilter_NoSort() {
        // Given
        Restaurant nearKorean = createRestaurant("가까운한식", 4.2, 10, "한식", 37.5506, 126.9400);
        Restaurant farKorean = createRestaurant("먼한식", 4.7, 15, "한식", 37.5600, 126.9400);
        Restaurant nearJapanese = createRestaurant("가까운일식", 4.9, 20, "일식", 37.5507, 126.9400);
        restaurantRepository.saveAll(List.of(nearKorean, farKorean, nearJapanese));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setCategory("한식");
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setRadius(500);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("가까운한식");
        assertThat(results.get(0).getCategory()).isEqualTo("한식");
    }

    @Test
    @DisplayName("13. 거리+카테고리필터 & 평점순 정렬: 거리 범위 내 해당 카테고리 식당 평점 내림차순")
    void search_DistanceAndCategoryFilter_SortByRating() {
        // Given
        Restaurant r1 = createRestaurant("한식1", 4.2, 10, "한식", 37.5506, 126.9400);
        Restaurant r2 = createRestaurant("한식2", 4.7, 15, "한식", 37.5511, 126.9400);
        Restaurant farKorean = createRestaurant("먼한식", 4.9, 20, "한식", 37.5600, 126.9400);
        Restaurant nearJapanese = createRestaurant("일식", 4.9, 25, "일식", 37.5507, 126.9400);
        restaurantRepository.saveAll(List.of(r1, r2, farKorean, nearJapanese));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setCategory("한식");
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setRadius(1000);
        request.setSortBy(SortBy.RATING);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStats().getRating()).isEqualTo(4.7);
        assertThat(results.get(1).getStats().getRating()).isEqualTo(4.2);
        assertThat(results).extracting(Restaurant::getCategory).containsOnly("한식");
    }

    @Test
    @DisplayName("14. 거리+카테고리필터 & 인기순 정렬: 거리 범위 내 해당 카테고리 식당 리뷰 개수 내림차순")
    void search_DistanceAndCategoryFilter_SortByPopular() {
        // Given
        Restaurant r1 = createRestaurant("한식1", 4.0, 5, "한식", 37.5506, 126.9400);
        Restaurant r2 = createRestaurant("한식2", 4.7, 15, "한식", 37.5507, 126.9400);
        Restaurant r3 = createRestaurant("일식", 4.9, 20, "일식", 37.5508, 126.9400);
        restaurantRepository.saveAll(List.of(r1, r2, r3));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setCategory("한식");
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);
        request.setRadius(500);
        request.setSortBy(SortBy.POPULAR);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStats().getReviewCount()).isEqualTo(15);
        assertThat(results.get(1).getStats().getReviewCount()).isEqualTo(5);
        assertThat(results).extracting(Restaurant::getCategory).containsOnly("한식");
    }

    // ========== 예외 케이스 3개 ==========

    @Test
    @DisplayName("예외1. 위도/경도 미제공 - 거리순 정렬 시 예외 발생")
    void exception_MissingCoordinates_DistanceSort() {
        // Given
        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setSortBy(SortBy.DISTANCE);

        // When & Then
        assertThatThrownBy(() -> restaurantService.findRestaurants(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("위도/경도");
    }

    @Test
    @DisplayName("예외1. 위도/경도 미제공 - 거리필터 시 예외 발생")
    void exception_MissingCoordinates_DistanceFilter() {
        // Given
        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setRadius(1000);

        // When & Then
        assertThatThrownBy(() -> restaurantService.findRestaurants(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("위도/경도");
    }

    @Test
    @DisplayName("예외2. 비활성 식당은 조회되지 않음")
    void exception_InactiveRestaurants_NotIncluded() {
        // Given
        Restaurant active = createRestaurant("활성", 4.5, 10, "한식", 37.5515, 126.9410);
        Restaurant inactive = createRestaurant("비활성", 4.9, 15, "한식", 37.5516, 126.9411);
        inactive.setActive(false);
        restaurantRepository.saveAll(List.of(active, inactive));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("활성");
        assertThat(results.get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("예외3. 존재하지 않는 카테고리 조회 시 빈 리스트 반환")
    void exception_NonExistentCategory_ReturnsEmptyList() {
        // Given
        Restaurant korean = createRestaurant("한식", 4.5, 10, "한식", 37.5515, 126.9410);
        Restaurant japanese = createRestaurant("일식", 4.8, 15, "일식", 37.5516, 126.9411);
        restaurantRepository.saveAll(List.of(korean, japanese));

        RestaurantSearchRequest request = new RestaurantSearchRequest();
        request.setCategory("존재하지않는카테고리");
        request.setLatitude(BASE_LAT);
        request.setLongitude(BASE_LNG);

        // When
        List<Restaurant> results = restaurantService.findRestaurants(request);

        // Then
        assertThat(results).isEmpty();
    }

    // ========== Helper 메서드 ==========
    private Restaurant createRestaurant(String name, double rating, int reviewCount,
                                        String category, double lat, double lng) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setType("ON_CAMPUS");
        restaurant.setCategory(category);
        restaurant.setAddress("테스트 주소");
        restaurant.setActive(true);
        restaurant.setLocation(new GeoJsonPoint(lng, lat));

        RestaurantStats stats = new RestaurantStats();
        stats.setRating(rating);
        stats.setReviewCount(reviewCount);
        stats.setLikeCount(0);
        restaurant.setStats(stats);

        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());

        return restaurant;
    }
}