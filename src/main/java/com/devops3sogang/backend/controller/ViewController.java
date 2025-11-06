package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Restaurant;
import com.devops3sogang.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Tag(name = "View Controller", description = "뷰 전용 API")
public class ViewController {

    private final RestaurantService restaurantService;

    /**
     * 맛집 목록 조회 페이지를 반환합니다.
     * @param model HTML에 데이터를 전달하기 위한 객체
     * @return "restaurants" -> resources/templates/restaurants.html 파일을 찾아 렌더링
     */
    @GetMapping("/restaurants-view")
    public String showRestaurantsPage(Model model) {
        // 1. 서비스에서 모든 맛집 목록을 가져옵니다.
        List<Restaurant> restaurantList = restaurantService.findRestaurants(null, null, null, null, null);

        // 2. 모델(Model)에 "restaurants"라는 이름으로 맛집 목록 데이터를 추가합니다.
        model.addAttribute("restaurants", restaurantList);

        // 3. 보여줄 HTML 파일의 이름을 반환합니다.
        return "restaurants";
    }
}