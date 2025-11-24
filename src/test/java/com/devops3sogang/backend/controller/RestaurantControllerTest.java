package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.RestaurantStats;
import com.devops3sogang.backend.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private static final double BASE_LAT = 37.5502;
    private static final double BASE_LNG = 126.9410;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
    }

    // ========== 정상 케이스 14개 ==========

    @Test
    @DisplayName("1. 필터X & 정렬기준X: 모든 식당 랜덤(DB 순서)")
    void search_NoFilter_NoSort() throws Exception {
        // Given
        Restaurant r1 = createRestaurant("식당1", 4.5, 10, "한식", 37.5515, 126.9410);
        Restaurant r2 = createRestaurant("식당2", 4.8, 20, "일식", 37.5516, 126.9411);
        restaurantRepository.saveAll(List.of(r1, r2));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("식당1", "식당2")));
    }

    @Test
    @DisplayName("2. 필터X & 거리순 정렬: 모든 식당 거리 오름차순")
    void search_NoFilter_SortByDistance() throws Exception {
        // Given
        Restaurant near = createRestaurant("가까운", 4.0, 10, "한식", 37.5506, 126.9410);
        Restaurant medium = createRestaurant("중간", 4.5, 15, "일식", 37.5511, 126.9410);
        Restaurant far = createRestaurant("먼", 4.9, 20, "중식", 37.5520, 126.9410);
        restaurantRepository.saveAll(List.of(far, near, medium));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("sortBy", "distance"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("가까운"))
                .andExpect(jsonPath("$[1].name").value("중간"))
                .andExpect(jsonPath("$[2].name").value("먼"));
    }

    @Test
    @DisplayName("3. 필터X & 평점순 정렬: 모든 식당 평점 내림차순")
    void search_NoFilter_SortByRating() throws Exception {
        // Given
        Restaurant low = createRestaurant("낮은평점", 4.0, 10, "한식", 37.5515, 126.9410);
        Restaurant high = createRestaurant("높은평점", 4.9, 15, "일식", 37.5516, 126.9411);
        Restaurant medium = createRestaurant("중간평점", 4.5, 20, "중식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(low, high, medium));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("sortBy", "rating"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].stats.rating").value(4.9))
                .andExpect(jsonPath("$[1].stats.rating").value(4.5))
                .andExpect(jsonPath("$[2].stats.rating").value(4.0));
    }

    @Test
    @DisplayName("4. 필터X & 인기순 정렬: 모든 식당 리뷰 개수 내림차순")
    void search_NoFilter_SortByPopular() throws Exception {
        // Given
        Restaurant r1 = createRestaurant("식당1", 4.5, 5, "한식", 37.5515, 126.9410);
        Restaurant r2 = createRestaurant("식당2", 4.7, 20, "일식", 37.5516, 126.9411);
        Restaurant r3 = createRestaurant("식당3", 4.2, 12, "중식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(r1, r2, r3));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("sortBy", "POPULAR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].stats.reviewCount").value(20))
                .andExpect(jsonPath("$[1].stats.reviewCount").value(12))
                .andExpect(jsonPath("$[2].stats.reviewCount").value(5));
    }

    @Test
    @DisplayName("5. 거리필터 & 정렬기준X: 거리 범위 내 식당 거리 오름차순")
    void search_DistanceFilter_NoSort() throws Exception {
        // Given
        Restaurant near = createRestaurant("가까운", 4.0, 10, "한식", 37.5506, 126.9410);
        Restaurant far = createRestaurant("먼", 4.8, 15, "일식", 37.5600, 126.9410);
        restaurantRepository.saveAll(List.of(near, far));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("radius", "500"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("가까운"));
    }

    @Test
    @DisplayName("6. 거리필터 & 평점순 정렬: 거리 범위 내 식당 평점 내림차순")
    void search_DistanceFilter_SortByRating() throws Exception {
        // Given
        Restaurant lowRating = createRestaurant("낮은평점", 4.0, 10, "한식", 37.5506, 126.9410);
        Restaurant highRating = createRestaurant("높은평점", 4.8, 15, "일식", 37.5511, 126.9410);
        Restaurant far = createRestaurant("먼", 4.9, 20, "중식", 37.5600, 126.9410);
        restaurantRepository.saveAll(List.of(lowRating, highRating, far));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("radius", "1000")
                        .param("sortBy", "rating"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].stats.rating").value(4.8))
                .andExpect(jsonPath("$[1].stats.rating").value(4.0));
    }

    @Test
    @DisplayName("7. 거리필터 & 인기순 정렬: 거리 범위 내 식당 리뷰 개수 내림차순")
    void search_DistanceFilter_SortByPopular() throws Exception {
        // Given
        Restaurant lowReview = createRestaurant("리뷰적음", 4.0, 5, "한식", 37.5506, 126.9410);
        Restaurant highReview = createRestaurant("리뷰많음", 4.2, 15, "일식", 37.5507, 126.9410);
        Restaurant far = createRestaurant("먼", 4.9, 100, "중식", 37.5600, 126.9410);
        restaurantRepository.saveAll(List.of(lowReview, highReview, far));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("radius", "500")
                        .param("sortBy", "POPULAR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].stats.reviewCount").value(15))
                .andExpect(jsonPath("$[1].stats.reviewCount").value(5));
    }

    @Test
    @DisplayName("8. 카테고리필터 & 정렬기준X: 해당 카테고리 식당 랜덤(DB 순서)")
    void search_CategoryFilter_NoSort() throws Exception {
        // Given
        Restaurant korean1 = createRestaurant("한식1", 4.5, 10, "한식", 37.5515, 126.9410);
        Restaurant korean2 = createRestaurant("한식2", 4.3, 15, "한식", 37.5516, 126.9411);
        Restaurant japanese = createRestaurant("일식", 4.8, 20, "일식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(korean1, korean2, japanese));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("category", "한식")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", everyItem(is("한식"))));
    }

    @Test
    @DisplayName("9. 카테고리필터 & 거리순 정렬: 해당 카테고리 식당 거리 오름차순")
    void search_CategoryFilter_SortByDistance() throws Exception {
        // Given
        Restaurant nearKorean = createRestaurant("가까운한식", 4.2, 10, "한식", 37.5506, 126.9410);
        Restaurant farKorean = createRestaurant("먼한식", 4.7, 15, "한식", 37.5520, 126.9410);
        Restaurant japanese = createRestaurant("일식", 4.9, 20, "일식", 37.5505, 126.9410);
        restaurantRepository.saveAll(List.of(farKorean, nearKorean, japanese));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("category", "한식")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("sortBy", "distance"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("가까운한식"))
                .andExpect(jsonPath("$[1].name").value("먼한식"))
                .andExpect(jsonPath("$[*].category", everyItem(is("한식"))));
    }

    @Test
    @DisplayName("10. 카테고리필터 & 평점순 정렬: 해당 카테고리 식당 평점 내림차순")
    void search_CategoryFilter_SortByRating() throws Exception {
        // Given
        Restaurant korean1 = createRestaurant("한식1", 4.3, 10, "한식", 37.5515, 126.9410);
        Restaurant korean2 = createRestaurant("한식2", 4.7, 15, "한식", 37.5516, 126.9411);
        Restaurant japanese = createRestaurant("일식", 4.9, 20, "일식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(korean1, korean2, japanese));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("category", "한식")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("sortBy", "rating"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].stats.rating").value(4.7))
                .andExpect(jsonPath("$[1].stats.rating").value(4.3))
                .andExpect(jsonPath("$[*].category", everyItem(is("한식"))));
    }

    @Test
    @DisplayName("11. 카테고리필터 & 인기순 정렬: 해당 카테고리 식당 리뷰 개수 내림차순")
    void search_CategoryFilter_SortByPopular() throws Exception {
        // Given
        Restaurant k1 = createRestaurant("한식1", 4.0, 3, "한식", 37.5515, 126.9410);
        Restaurant k2 = createRestaurant("한식2", 4.5, 10, "한식", 37.5516, 126.9411);
        Restaurant j1 = createRestaurant("일식", 4.8, 20, "일식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(k1, k2, j1));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("category", "한식")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("sortBy", "POPULAR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].stats.reviewCount").value(10))
                .andExpect(jsonPath("$[1].stats.reviewCount").value(3))
                .andExpect(jsonPath("$[*].category", everyItem(is("한식"))));
    }

    @Test
    @DisplayName("12. 거리+카테고리필터 & 정렬기준X: 거리 범위 내 해당 카테고리 식당 거리 오름차순")
    void search_DistanceAndCategoryFilter_NoSort() throws Exception {
        // Given
        Restaurant nearKorean = createRestaurant("가까운한식", 4.2, 10, "한식", 37.5506, 126.9410);
        Restaurant farKorean = createRestaurant("먼한식", 4.7, 15, "한식", 37.5600, 126.9410);
        Restaurant nearJapanese = createRestaurant("가까운일식", 4.9, 20, "일식", 37.5507, 126.9410);
        restaurantRepository.saveAll(List.of(nearKorean, farKorean, nearJapanese));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("category", "한식")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("radius", "500"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("가까운한식"))
                .andExpect(jsonPath("$[0].category").value("한식"));
    }

    @Test
    @DisplayName("13. 거리+카테고리필터 & 평점순 정렬: 거리 범위 내 해당 카테고리 식당 평점 내림차순")
    void search_DistanceAndCategoryFilter_SortByRating() throws Exception {
        // Given
        Restaurant r1 = createRestaurant("한식1", 4.2, 10, "한식", 37.5506, 126.9410);
        Restaurant r2 = createRestaurant("한식2", 4.7, 15, "한식", 37.5511, 126.9410);
        Restaurant farKorean = createRestaurant("먼한식", 4.9, 20, "한식", 37.5600, 126.9410);
        Restaurant nearJapanese = createRestaurant("일식", 4.9, 25, "일식", 37.5507, 126.9410);
        restaurantRepository.saveAll(List.of(r1, r2, farKorean, nearJapanese));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("category", "한식")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("radius", "1000")
                        .param("sortBy", "rating"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].stats.rating").value(4.7))
                .andExpect(jsonPath("$[1].stats.rating").value(4.2))
                .andExpect(jsonPath("$[*].category", everyItem(is("한식"))));
    }

    @Test
    @DisplayName("14. 거리+카테고리필터 & 인기순 정렬: 거리 범위 내 해당 카테고리 식당 리뷰 개수 내림차순")
    void search_DistanceAndCategoryFilter_SortByPopular() throws Exception {
        // Given
        Restaurant r1 = createRestaurant("한식1", 4.0, 5, "한식", 37.5506, 126.9410);
        Restaurant r2 = createRestaurant("한식2", 4.7, 15, "한식", 37.5507, 126.9410);
        Restaurant r3 = createRestaurant("일식", 4.9, 20, "일식", 37.5508, 126.9410);
        restaurantRepository.saveAll(List.of(r1, r2, r3));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("category", "한식")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG))
                        .param("radius", "500")
                        .param("sortBy", "POPULAR"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].stats.reviewCount").value(15))
                .andExpect(jsonPath("$[1].stats.reviewCount").value(5))
                .andExpect(jsonPath("$[*].category", everyItem(is("한식"))));
    }

    // ========== 예외 케이스 3개 ==========

    @Test
    @DisplayName("예외1. 위도/경도 미제공 - 거리순 정렬 시 400 에러")
    void exception_MissingCoordinates_DistanceSort() throws Exception {
        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("sortBy", "distance"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예외1. 위도/경도 미제공 - 거리필터 시 400 에러")
    void exception_MissingCoordinates_DistanceFilter() throws Exception {
        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("radius", "1000"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("예외2. 비활성 식당은 조회되지 않음")
    void exception_InactiveRestaurants_NotIncluded() throws Exception {
        // Given
        Restaurant active = createRestaurant("활성", 4.5, 10, "한식", 37.5515, 126.9410);
        Restaurant inactive = createRestaurant("비활성", 4.9, 15, "한식", 37.5516, 126.9411);
        inactive.setActive(false);
        restaurantRepository.saveAll(List.of(active, inactive));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("활성"));
    }

    @Test
    @DisplayName("예외3. 존재하지 않는 카테고리 조회 시 빈 리스트 반환")
    void exception_NonExistentCategory_ReturnsEmptyList() throws Exception {
        // Given
        Restaurant korean = createRestaurant("한식", 4.5, 10, "한식", 37.5515, 126.9410);
        Restaurant japanese = createRestaurant("일식", 4.8, 15, "일식", 37.5516, 126.9411);
        restaurantRepository.saveAll(List.of(korean, japanese));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("category", "존재하지않는카테고리")
                        .param("lat", String.valueOf(BASE_LAT))
                        .param("lng", String.valueOf(BASE_LNG)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
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
        stats.setLikeCount(5);
        restaurant.setStats(stats);

        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());

        return restaurant;
    }
}