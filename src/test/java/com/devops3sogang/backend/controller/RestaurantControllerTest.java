package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.GeoJsonPoint;
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

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /restaurants - 전체 조회")
    void getRestaurants_All() throws Exception {
        // Given
        Restaurant r1 = createRestaurant("식당1", 4.5, "ON_CAMPUS", "한식", 37.5515, 126.9410);
        Restaurant r2 = createRestaurant("식당2", 4.8, "ON_CAMPUS", "일식", 37.5516, 126.9411);
        restaurantRepository.saveAll(List.of(r1, r2));

        // When & Then
        mockMvc.perform(get("/restaurants"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].stats.rating").exists());
    }

    @Test
    @DisplayName("GET /restaurants?sortBy=rating - 평점순 정렬")
    void getRestaurants_SortByRating() throws Exception {
        // Given
        Restaurant low = createRestaurant("낮은평점", 4.0, "ON_CAMPUS", "한식", 37.5515, 126.9410);
        Restaurant high = createRestaurant("높은평점", 4.9, "ON_CAMPUS", "일식", 37.5516, 126.9411);
        Restaurant medium = createRestaurant("중간평점", 4.5, "OFF_CAMPUS", "중식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(low, high, medium));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("sortBy", "rating"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].stats.rating").value(4.9))
                .andExpect(jsonPath("$[1].stats.rating").value(4.5))
                .andExpect(jsonPath("$[2].stats.rating").value(4.0));
    }

    @Test
    @DisplayName("GET /restaurants?type=ON_CAMPUS&sortBy=rating - 타입 필터 + 평점순")
    void getRestaurants_FilterByType_SortByRating() throws Exception {
        // Given
        Restaurant onCampus1 = createRestaurant("교내1", 4.3, "ON_CAMPUS", "한식", 37.5515, 126.9410);
        Restaurant onCampus2 = createRestaurant("교내2", 4.8, "ON_CAMPUS", "일식", 37.5516, 126.9411);
        Restaurant offCampus = createRestaurant("교외", 4.9, "OFF_CAMPUS", "중식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(onCampus1, onCampus2, offCampus));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("type", "ON_CAMPUS")
                        .param("sortBy", "rating"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].stats.rating").value(4.8))
                .andExpect(jsonPath("$[1].stats.rating").value(4.3))
                .andExpect(jsonPath("$[*].type", everyItem(is("ON_CAMPUS"))));
    }

    @Test
    @DisplayName("GET /restaurants?category=한식&sortBy=rating - 카테고리 필터 + 평점순")
    void getRestaurants_FilterByCategory_SortByRating() throws Exception {
        // Given
        Restaurant korean1 = createRestaurant("한식1", 4.2, "ON_CAMPUS", "한식", 37.5515, 126.9410);
        Restaurant korean2 = createRestaurant("한식2", 4.7, "OFF_CAMPUS", "한식", 37.5516, 126.9411);
        Restaurant japanese = createRestaurant("일식", 4.9, "ON_CAMPUS", "일식", 37.5517, 126.9412);
        restaurantRepository.saveAll(List.of(korean1, korean2, japanese));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("category", "한식")
                        .param("sortBy", "rating"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].stats.rating").value(4.7))
                .andExpect(jsonPath("$[1].stats.rating").value(4.2))
                .andExpect(jsonPath("$[*].category", everyItem(is("한식"))));
    }

    @Test
    @DisplayName("GET /restaurants?lat=37.5502&lng=126.9410&radius=500 - 거리 기반 검색")
    void getRestaurants_DistanceSearch() throws Exception {
        // Given
        double baseLat = 37.5502;
        double baseLng = 126.9410;

        Restaurant near = createRestaurant("가까운", 4.0, "ON_CAMPUS", "한식", 37.5506, 126.9410);
        Restaurant far = createRestaurant("먼", 4.8, "OFF_CAMPUS", "일식", 37.5600, 126.9410); // ~1km
        restaurantRepository.saveAll(List.of(near, far));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", String.valueOf(baseLat))
                        .param("lng", String.valueOf(baseLng))
                        .param("radius", "500"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("가까운"));
    }

    @Test
    @DisplayName("GET /restaurants?lat=37.5502&lng=126.9410&sortBy=rating - 거리검색 + 평점순")
    void getRestaurants_DistanceSearch_SortByRating() throws Exception {
        // Given
        double baseLat = 37.5502;
        double baseLng = 126.9410;

        Restaurant near = createRestaurant("가까운저평점", 4.0, "ON_CAMPUS", "한식", 37.5506, 126.9410);
        Restaurant medium = createRestaurant("중간고평점", 4.8, "ON_CAMPUS", "일식", 37.5511, 126.9410);
        restaurantRepository.saveAll(List.of(near, medium));

        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", String.valueOf(baseLat))
                        .param("lng", String.valueOf(baseLng))
                        .param("radius", "1000")
                        .param("sortBy", "rating"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].stats.rating").value(4.8))
                .andExpect(jsonPath("$[1].stats.rating").value(4.0));
    }

    @Test
    @DisplayName("GET /restaurants?lat=37.5502 - lat만 제공 시 400 에러")
    void getRestaurants_OnlyLatitude_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lat", "37.5502"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /restaurants?lng=126.9410 - lng만 제공 시 400 에러")
    void getRestaurants_OnlyLongitude_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/restaurants")
                        .param("lng", "126.9410"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /restaurants - 기본 정렬(sortBy 미지정)")
    void getRestaurants_DefaultSort() throws Exception {
        // Given
        Restaurant r1 = createRestaurant("식당1", 4.5, "ON_CAMPUS", "한식", 37.5515, 126.9410);
        Restaurant r2 = createRestaurant("식당2", 4.8, "ON_CAMPUS", "일식", 37.5516, 126.9411);
        restaurantRepository.saveAll(List.of(r1, r2));

        // When & Then
        mockMvc.perform(get("/restaurants"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /restaurants/{restaurantId} - 상세 조회")
    void getRestaurantById() throws Exception {
        // Given
        Restaurant restaurant = createRestaurant("테스트식당", 4.5, "ON_CAMPUS", "한식", 37.5515, 126.9410);
        Restaurant saved = restaurantRepository.save(restaurant);

        // When & Then
        mockMvc.perform(get("/restaurants/{restaurantId}", saved.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트식당"))
                .andExpect(jsonPath("$.stats.rating").value(4.5))
                .andExpect(jsonPath("$.type").value("ON_CAMPUS"))
                .andExpect(jsonPath("$.category").value("한식"));
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
        location.setCoordinates(new double[]{lng, lat});
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