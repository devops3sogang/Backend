package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.DailyMenu;
import com.devops3sogang.backend.document.Meal;
import com.devops3sogang.backend.document.OnCampusMenu;
import com.devops3sogang.backend.dto.crawler.CrawledMenuResponse;
import com.devops3sogang.backend.dto.crawler.DailyMenuData;
import com.devops3sogang.backend.dto.crawler.MenuInfo;
import com.devops3sogang.backend.repository.OnCampusMenuRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuCrawlingService {

    private final OnCampusMenuRepository onCampusMenuRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 매주 월요일 새벽 4시에 주간 메뉴 크롤링을 실행하고 DB에 저장합니다.
     * @return 성공 시 true, 실패 시 false
     */
    @Scheduled(cron = "0 0 4 * * MON")
    public boolean crawlAndSaveWeeklyMenu() {
        log.info("주간 메뉴 크롤링 스케줄을 시작합니다...");

        try {
            // 1. Python 스크립트 실행
            // 👇 "python" 대신 가상 환경 내부의 python 실행 파일 경로를 직접 지정합니다.
            String os = System.getProperty("os.name").toLowerCase();
            String pythonPath;

            if (os.contains("win")) {
                pythonPath = "../crawling/venv/Scripts/python.exe";  // Windows
            } else {
                pythonPath = "../crawling/venv/bin/python";          // macOS / Linux
            }

            ProcessBuilder pb = new ProcessBuilder(
                pythonPath,
                "../crawling/crawler.py"
            );

            Map<String, String> env = pb.environment();
            env.put("PYTHONIOENCODING", "UTF-8");

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Python 스크립트의 출력(에러 메시지 포함)을 읽어서 로그로 남김
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[Python] {}", line);
                }
            }

            // 스크립트가 끝날 때까지 대기하고 종료 코드 확인
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Python 크롤러 스크립트 실행에 실패했습니다. 종료 코드: {}", exitCode);
                return false; // 실패 시 false 반환
            }
            log.info("Python 크롤러 스크립트 실행이 완료되었습니다.");

            // 2. 생성된 JSON 파일 확인 및 읽기
            File jsonFile = new File("menu.json");
            if (!jsonFile.exists()) {
                log.error("menu.json 파일이 생성되지 않았습니다. 크롤러 스CRIPT에 문제가 있을 수 있습니다.");
                return false; // 파일이 없으면 false 반환
            }

            CrawledMenuResponse response = objectMapper.readValue(jsonFile, CrawledMenuResponse.class);
            log.info("menu.json 파일 파싱이 완료되었습니다.");

            // 3. Crawled DTO -> OnCampusMenu Document로 변환
            OnCampusMenu onCampusMenu = new OnCampusMenu();
            List<DailyMenu> dailyMenus = new ArrayList<>();

            if (response.getData() == null || response.getData().getMenuList() == null) {
                log.warn("크롤링된 데이터에 menuList가 없습니다.");
                return false;
            }

            for (DailyMenuData dailyData : response.getData().getMenuList()) {
                DailyMenu dailyMenu = new DailyMenu();
                LocalDate date = LocalDate.parse(dailyData.getMenuDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                dailyMenu.setDate(date.toString());
                dailyMenu.setDayOfWeek(date.getDayOfWeek().toString());

                List<Meal> meals = new ArrayList<>();
                for (MenuInfo menuInfo : dailyData.getMenuInfo()) {
                    Meal meal = new Meal();
                    meal.setCorner(menuInfo.getCategory());
                    // <br> 태그 기준 분리 및 불필요한 공백/탭 제거
                    List<String> items = Arrays.stream(menuInfo.getMenu().split("<br>"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                    meal.setItems(items);
                    meals.add(meal);
                }
                dailyMenu.setMeals(meals);
                dailyMenus.add(dailyMenu);
            }

            // 4. MongoDB에 저장
            if (!dailyMenus.isEmpty()) {
                LocalDate firstDate = LocalDate.parse(dailyMenus.get(0).getDate());
                LocalDate weekStartDate = firstDate.with(DayOfWeek.MONDAY);
                onCampusMenu.setWeekStartDate(weekStartDate.toString());
                onCampusMenu.setDailyMenus(dailyMenus);
                onCampusMenu.setRestaurantName("우정원"); // 식당 이름 하드코딩

                // 이미 해당 주 데이터가 있으면 ID를 설정하여 덮어쓰기(Update)
                onCampusMenuRepository.findByWeekStartDate(weekStartDate.toString())
                        .ifPresent(existingMenu -> onCampusMenu.setId(existingMenu.getId()));

                onCampusMenuRepository.save(onCampusMenu);
                log.info("{} 주차 메뉴가 성공적으로 DB에 저장/업데이트 되었습니다.", weekStartDate);
            } else {
                log.warn("DB에 저장할 메뉴 데이터가 없습니다.");
            }

            return true; // 모든 과정 성공 시 true 반환

        } catch (Exception e) {
            log.error("메뉴 크롤링 및 저장 과정에서 오류가 발생했습니다.", e);
            return false; // 예외 발생 시 false 반환
        }
    }
}