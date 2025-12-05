package com.devops3sogang.backend.config;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.repository.RestaurantRepository;
import com.devops3sogang.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

@Configuration
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository,
                      RestaurantRepository restaurantRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 관리자 계정 생성
        String adminEmail = "admin@sogang.ac.kr";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setNickname("관리자");
            admin.setPasswordHash(passwordEncoder.encode("Devops3sogang!"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("관리자 계정 생성 완료: " + adminEmail);
        }

        // MAIN_CAMPUS 학생식당 생성
        String mainCampusId = "MAIN_CAMPUS";
        if (restaurantRepository.findById(mainCampusId).isEmpty()) {
            Restaurant mainCampus = new Restaurant();
            mainCampus.setId(mainCampusId);
            mainCampus.setName("서강대학교 우정원 학생식당");
            mainCampus.setMenu(new ArrayList<>()); // 초기 메뉴 비움
            restaurantRepository.save(mainCampus);
            System.out.println("MAIN_CAMPUS 식당 생성 완료");
        }
    }
}