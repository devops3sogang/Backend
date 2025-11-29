package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.OnCampusMenu;
import com.devops3sogang.backend.repository.OnCampusMenuRepository;
import com.devops3sogang.backend.dto.OnCampusMenuResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/on-campus-menus")
@RequiredArgsConstructor
@Tag(name = "OnCampusMenu Controller", description = "교내 식당 전용 API")
public class OnCampusMenuController {

    private final OnCampusMenuRepository onCampusMenuRepository;

    /**
     * 특정 날짜가 포함된 주(Week)의 교내 식당 메뉴를 조회합니다.
     * GET /on-campus-menus?date={YYYY-MM-DD}
     * @param date 조회할 날짜 (YYYY-MM-DD 형식)
     * @return 해당 주의 OnCampusMenu Document
     */
    @GetMapping
    public ResponseEntity<OnCampusMenuResponse> getMenuByDate(@RequestParam String date) {
        List<OnCampusMenu> menus = onCampusMenuRepository.findByDailyMenusDate(date);

        if (menus.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OnCampusMenu menu = menus.get(0);
        OnCampusMenuResponse response = new OnCampusMenuResponse();
        response.setId(menu.getId());
        response.setRestaurantId(menu.getRestaurantId());
        response.setRestaurantName(menu.getRestaurantName());
        response.setWeekStartDate(menu.getWeekStartDate());
        response.setDailyMenus(menu.getDailyMenus());

        return ResponseEntity.ok(response);
    }
}