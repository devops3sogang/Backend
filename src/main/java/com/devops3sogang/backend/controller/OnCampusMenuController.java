package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.OnCampusMenu;
import com.devops3sogang.backend.service.OnCampusMenuService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/on-campus-menus")
@RequiredArgsConstructor
public class OnCampusMenuController {

    private final OnCampusMenuService onCampusMenuService;

    /**
     * 특정 날짜가 포함된 주(Week)의 교내 식당 메뉴를 조회합니다.
     * GET /on-campus-menus?date={YYYY-MM-DD}
     * @param date 조회할 날짜 (YYYY-MM-DD 형식)
     * @return 해당 주의 OnCampusMenu Document
     */
    @GetMapping
    public ResponseEntity<OnCampusMenu> getMenuByDate(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식)", required = true, example = "2025-10-06")
            @RequestParam("date") String date) {
        // 입력받은 날짜를 기준으로 해당 주의 시작일(월요일)을 계산합니다.
        LocalDate requestedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate weekStartDate = requestedDate.with(DayOfWeek.MONDAY);
        String weekStartDateString = weekStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // 주의 시작일을 기준으로 메뉴를 서비스에서 조회합니다.
        OnCampusMenu menu = onCampusMenuService.findMenuByWeekStartDate(weekStartDateString);
        return ResponseEntity.ok(menu);
    }
}