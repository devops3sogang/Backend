package com.devops3sogang.backend.config;

import com.devops3sogang.backend.document.*;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import com.devops3sogang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.UUID;
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
        if (restaurantRepository.count() > 0) {
            System.out.println("기존 데이터가 존재하여 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("DB 비어있음 → Seed 데이터 생성");

        User testUser = createTestUser();
        User adminUser = createAdminUser();

        Restaurant restaurant1 = createRestaurantKimchi();
        Restaurant restaurant2 = createRestaurantTonkatsu();

        createReviews(testUser, restaurant1, restaurant2);

        System.out.println("Seed 완료");
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("test@sogang.ac.kr");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setNickname("테스트유저");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private User createAdminUser() {
        User admin = new User();
        admin.setEmail("admin@sogang.ac.kr");
        admin.setPasswordHash(passwordEncoder.encode("admin1234"));
        admin.setNickname("관리자");
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(admin);
    }

    private Restaurant createRestaurantKimchi() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("맛있는 김치찌개");
        restaurant.setType("OFF_CAMPUS");
        restaurant.setCategory("한식");
        restaurant.setAddress("서울시 마포구 백범로 35");
        restaurant.setLocation(new GeoJsonPoint(126.9370, 37.5509));
        restaurant.setImageUrl("https://example.com/images/kimchi-restaurant.jpg");
        restaurant.setActive(true);

        restaurant.setStats(new RestaurantStats(0.0, 0, 0));

        MenuItem menu1 = new MenuItem();
        menu1.setId(UUID.randomUUID().toString());
        menu1.setName("김치찌개");
        menu1.setPrice(8000);

        MenuItem menu2 = new MenuItem();
        menu2.setId(UUID.randomUUID().toString());
        menu2.setName("된장찌개");
        menu2.setPrice(8000);

        MenuItem menu3 = new MenuItem();
        menu3.setId(UUID.randomUUID().toString());
        menu3.setName("제육볶음");
        menu3.setPrice(9000);

        restaurant.setMenu(Arrays.asList(menu1, menu2, menu3));
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());
        return restaurantRepository.save(restaurant);
    }

    private Restaurant createRestaurantTonkatsu() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("서강 돈까스");
        restaurant.setType("OFF_CAMPUS");
        restaurant.setCategory("일식");
        restaurant.setAddress("서울시 마포구 신수동 1-1");
        restaurant.setLocation(new GeoJsonPoint(126.9390, 37.5515));
        restaurant.setImageUrl("https://example.com/images/tonkatsu-restaurant.jpg");
        restaurant.setActive(true);

        restaurant.setStats(new RestaurantStats(0.0, 0, 0));

        MenuItem menu1 = new MenuItem();
        menu1.setId(UUID.randomUUID().toString());
        menu1.setName("등심돈까스");
        menu1.setPrice(10000);

        MenuItem menu2 = new MenuItem();
        menu2.setId(UUID.randomUUID().toString());
        menu2.setName("치즈돈까스");
        menu2.setPrice(11000);

        MenuItem menu3 = new MenuItem();
        menu3.setId(UUID.randomUUID().toString());
        menu3.setName("카레돈까스");
        menu3.setPrice(10500);

        restaurant.setMenu(Arrays.asList(menu1, menu2, menu3));
        restaurant.setCreatedAt(LocalDateTime.now());
        restaurant.setUpdatedAt(LocalDateTime.now());
        return restaurantRepository.save(restaurant);
    }

    private void createReviews(User user, Restaurant r1, Restaurant r2) {

        // restaurant1 메뉴 ID 조회
        String kimchiId = r1.getMenu().get(0).getId();
        String porkId = r1.getMenu().get(2).getId();

        // restaurant2 메뉴 ID 조회
        String tonkatsuId = r2.getMenu().get(0).getId();
        String cheeseId = r2.getMenu().get(1).getId();

        Review review1 = buildReview(user, r1.getId(), List.of(kimchiId), 5, 4,
                "김치찌개가 정말 맛있어요! 국물이 깊고 진해요.");
        Review review2 = buildReview(user, r1.getId(), List.of(porkId), 4, 4,
                "제육볶음도 맛있네요. 가성비 좋아요!");
        Review review3 = buildReview(user, r2.getId(), List.of(tonkatsuId), 5, 5,
                "돈까스 바삭하고 고기도 두툼. 데이트하기 좋습니다.");
        Review review4 = buildReview(user, r2.getId(), List.of(cheeseId), 5, 4,
                "치즈돈까스 치즈가 쭉 늘어나요. 맛있지만 가격은 살짝 높음.");

        reviewRepository.saveAll(List.of(review1, review2, review3, review4));
    }

    private Review buildReview(User user, String restaurantId, List<String> menuIds,
                               int menuRatingValue, int restaurantRatingValue, String content) {
        Review review = new Review();
        review.setUserId(user.getId());
        review.setNickname(user.getNickname());

        ReviewTarget target = new ReviewTarget();
        target.setType(Type.RESTAURANT);
        target.setRestaurantId(restaurantId);
        target.setMenuIds(menuIds);
        review.setTarget(target);

        Rating rating = new Rating();
        Rating.MenuRating menuRating = new Rating.MenuRating();
        menuRating.setMenuId(menuIds.get(0));
        menuRating.setRating(menuRatingValue);
        rating.setMenuRatings(List.of(menuRating));
        rating.setRestaurantRating(restaurantRatingValue);
        review.setRating(rating);

        review.setContent(content);
        review.setImageUrls(null);
        review.setLikeCount(0);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        return review;
    }
}