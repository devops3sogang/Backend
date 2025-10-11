package com.devops3sogang.backend.config;

import com.devops3sogang.backend.document.*;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import com.devops3sogang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 레스토랑 데이터가 있으면 초기화하지 않음 (레스토랑 기준으로 체크)
        if (restaurantRepository.count() > 0) {
            System.out.println("데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("테스트 데이터를 초기화합니다...");

        // 1. 테스트 유저 생성
        User testUser = createTestUser();

        // 2. 가게 2곳 생성
        Restaurant restaurant1 = createRestaurant1();
        Restaurant restaurant2 = createRestaurant2();

        // 3. 리뷰 4개 생성 (각 가게에 2개씩)
        createReviews(testUser, restaurant1, restaurant2);

        System.out.println("테스트 데이터 초기화 완료!");
        System.out.println("- 유저: " + testUser.getEmail());
        System.out.println("- 가게 1: " + restaurant1.getName());
        System.out.println("- 가게 2: " + restaurant2.getName());
        System.out.println("- 리뷰: 4개");
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("test@sogang.ac.kr");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setNickname("테스트유저");
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Restaurant createRestaurant1() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("맛있는 김치찌개");
        restaurant.setType("restaurant");
        restaurant.setCategory("한식");
        restaurant.setAddress("서울시 마포구 백범로 35");

        // 위치 설정 (서강대 근처 좌표)
        GeoJsonPoint location = new GeoJsonPoint();
        location.setCoordinates(new double[] { 126.9370, 37.5509 });
        restaurant.setLocation(location);

        restaurant.setImageUrl("https://example.com/images/kimchi-restaurant.jpg");
        restaurant.setActive(true);

        // 통계 초기화
        RestaurantStats stats = new RestaurantStats();
        stats.setRating(0.0);
        stats.setReviewCount(0);
        stats.setLikeCount(0);
        restaurant.setStats(stats);

        // 메뉴 추가
        MenuItem menu1 = new MenuItem();
        menu1.setName("김치찌개");
        menu1.setPrice(8000);

        MenuItem menu2 = new MenuItem();
        menu2.setName("된장찌개");
        menu2.setPrice(8000);

        MenuItem menu3 = new MenuItem();
        menu3.setName("제육볶음");
        menu3.setPrice(9000);

        restaurant.setMenu(Arrays.asList(menu1, menu2, menu3));
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());

        return restaurantRepository.save(restaurant);
    }

    private Restaurant createRestaurant2() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("서강 돈까스");
        restaurant.setType("restaurant");
        restaurant.setCategory("일식");
        restaurant.setAddress("서울시 마포구 신수동 1-1");

        // 위치 설정 (서강대 근처 좌표)
        GeoJsonPoint location = new GeoJsonPoint();
        location.setCoordinates(new double[] { 126.9390, 37.5515 });
        restaurant.setLocation(location);

        restaurant.setImageUrl("https://example.com/images/tonkatsu-restaurant.jpg");
        restaurant.setActive(true);

        // 통계 초기화
        RestaurantStats stats = new RestaurantStats();
        stats.setRating(0.0);
        stats.setReviewCount(0);
        stats.setLikeCount(0);
        restaurant.setStats(stats);

        // 메뉴 추가
        MenuItem menu1 = new MenuItem();
        menu1.setName("등심돈까스");
        menu1.setPrice(10000);

        MenuItem menu2 = new MenuItem();
        menu2.setName("치즈돈까스");
        menu2.setPrice(11000);

        MenuItem menu3 = new MenuItem();
        menu3.setName("카레돈까스");
        menu3.setPrice(10500);

        restaurant.setMenu(Arrays.asList(menu1, menu2, menu3));
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());

        return restaurantRepository.save(restaurant);
    }

    private void createReviews(User user, Restaurant restaurant1, Restaurant restaurant2) {
        // 가게 1 리뷰 1
        Review review1 = new Review();
        review1.setUserId(user.getId());
        review1.setNickname(user.getNickname());

        ReviewTarget target1 = new ReviewTarget();
        target1.setType("restaurant");
        target1.setRestaurantId(restaurant1.getId());
        target1.setRestaurantName(restaurant1.getName());
        target1.setMenuItems("김치찌개");
        review1.setTarget(target1);

        Ratings ratings1 = new Ratings();
        ratings1.setTaste(5);
        ratings1.setPrice(4);
        ratings1.setAtmosphere(4);
        review1.setRatings(ratings1);

        review1.setContent("김치찌개가 정말 맛있어요! 국물이 깊고 진해요. 서강대 근처에서 최고입니다.");
        review1.setImageUrl("https://example.com/images/review1.jpg");
        review1.setLikeCount(2);
        review1.setCreatedAt(LocalDateTime.now().minusDays(5));
        review1.setUpdatedAt(LocalDateTime.now().minusDays(5));
        reviewRepository.save(review1);

        // 가게 1 리뷰 2
        Review review2 = new Review();
        review2.setUserId(user.getId());
        review2.setNickname(user.getNickname());

        ReviewTarget target2 = new ReviewTarget();
        target2.setType("restaurant");
        target2.setRestaurantId(restaurant1.getId());
        target2.setRestaurantName(restaurant1.getName());
        target2.setMenuItems("제육볶음");
        review2.setTarget(target2);

        Ratings ratings2 = new Ratings();
        ratings2.setTaste(4);
        ratings2.setPrice(5);
        ratings2.setAtmosphere(3);
        review2.setRatings(ratings2);

        review2.setContent("제육볶음도 맛있네요. 가성비 좋아요!");
        review2.setImageUrl(null);
        review2.setLikeCount(0);
        review2.setCreatedAt(LocalDateTime.now().minusDays(3));
        review2.setUpdatedAt(LocalDateTime.now().minusDays(3));
        reviewRepository.save(review2);

        // 가게 2 리뷰 1
        Review review3 = new Review();
        review3.setUserId(user.getId());
        review3.setNickname(user.getNickname());

        ReviewTarget target3 = new ReviewTarget();
        target3.setType("restaurant");
        target3.setRestaurantId(restaurant2.getId());
        target3.setRestaurantName(restaurant2.getName());
        target3.setMenuItems("등심돈까스");
        review3.setTarget(target3);

        Ratings ratings3 = new Ratings();
        ratings3.setTaste(5);
        ratings3.setPrice(4);
        ratings3.setAtmosphere(5);
        review3.setRatings(ratings3);

        review3.setContent("돈까스가 바삭하고 고기가 두툼해요. 분위기도 좋고 데이트하기 좋습니다.");
        review3.setImageUrl("https://example.com/images/review3.jpg");
        review3.setLikeCount(0);
        review3.setCreatedAt(LocalDateTime.now().minusDays(2));
        review3.setUpdatedAt(LocalDateTime.now().minusDays(2));
        reviewRepository.save(review3);

        // 가게 2 리뷰 2
        Review review4 = new Review();
        review4.setUserId(user.getId());
        review4.setNickname(user.getNickname());

        ReviewTarget target4 = new ReviewTarget();
        target4.setType("restaurant");
        target4.setRestaurantId(restaurant2.getId());
        target4.setRestaurantName(restaurant2.getName());
        target4.setMenuItems("치즈돈까스");
        review4.setTarget(target4);

        Ratings ratings4 = new Ratings();
        ratings4.setTaste(5);
        ratings4.setPrice(3);
        ratings4.setAtmosphere(4);
        review4.setRatings(ratings4);

        review4.setContent("치즈돈까스 치즈가 쭉쭉 늘어나요! 맛있습니다. 다만 가격이 조금 비싼 편.");
        review4.setImageUrl(null);
        review4.setLikeCount(0);
        review4.setCreatedAt(LocalDateTime.now().minusDays(1));
        review4.setUpdatedAt(LocalDateTime.now().minusDays(1));
        reviewRepository.save(review4);

        // 레스토랑 통계 업데이트
        updateRestaurantStats(restaurant1, Arrays.asList(review1, review2));
        updateRestaurantStats(restaurant2, Arrays.asList(review3, review4));
    }

    private void updateRestaurantStats(Restaurant restaurant, List<Review> reviews) {
        RestaurantStats stats = restaurant.getStats();
        stats.setReviewCount(reviews.size());

        // 평균 평점 계산 (맛, 가격, 분위기 평균)
        double avgRating = reviews.stream()
                .mapToDouble(r -> (r.getRatings().getTaste() +
                        r.getRatings().getPrice() +
                        r.getRatings().getAtmosphere()) / 3.0)
                .average()
                .orElse(0.0);
        stats.setRating(Math.round(avgRating * 10) / 10.0); // 소수점 첫째자리까지

        restaurantRepository.save(restaurant);
    }
}
