package com.devops3sogang.backend.util;

import com.devops3sogang.backend.repository.OnCampusMenuRepository; // Repository import 추가
import com.devops3sogang.backend.service.MenuCrawlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppStartupRunner implements ApplicationRunner {

    private final MenuCrawlingService menuCrawlingService;
    private final OnCampusMenuRepository onCampusMenuRepository; // Repository 주입

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. 오늘 날짜를 기준으로 이번 주 월요일 날짜를 계산합니다.
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        String currentWeekStartDate = monday.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // 2. DB에서 이번 주 메뉴 데이터가 이미 있는지 확인합니다.
        boolean menuExists = onCampusMenuRepository.findByWeekStartDate(currentWeekStartDate).isPresent();

        if (!menuExists) {
            // 3. 데이터가 없을 경우에만 크롤링을 실행합니다.
            log.info("이번 주 메뉴 데이터가 DB에 없습니다. 초기 데이터 크롤링을 실행합니다.");
            menuCrawlingService.crawlAndSaveWeeklyMenu();
        } else {
            // 4. 데이터가 이미 존재할 경우, 로그를 남기고 건너뜁니다.
            log.info("이번 주 메뉴 데이터가 이미 DB에 존재하므로, 초기 크롤링을 건너뜁니다.");
        }
    }
}