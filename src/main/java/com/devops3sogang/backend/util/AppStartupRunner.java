package com.devops3sogang.backend.util;

import com.devops3sogang.backend.repository.OnCampusMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppStartupRunner implements ApplicationRunner {

    private final OnCampusMenuRepository onCampusMenuRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        String currentWeekStartDate = monday.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String restaurantId = "MAIN_CAMPUS";

        boolean menuExists = !onCampusMenuRepository.findByRestaurantIdAndWeekStartDate(restaurantId, currentWeekStartDate).isEmpty();

        String enableCrawler = System.getenv("ENABLE_CRAWLER");
        if ("true".equalsIgnoreCase(enableCrawler) && !menuExists) {
            log.info("이번 주 메뉴 데이터가 DB에 없습니다. 초기 데이터 크롤링을 실행합니다.");

            URI uri = new URI("http://crawler:5000/run");
            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            try {
                con.setRequestMethod("POST");
                con.setConnectTimeout(5000);
                con.setReadTimeout(60000);
                int status = con.getResponseCode();
                if (status != 200) {
                    log.warn("Crawler 호출 실패, 상태 코드: {}", status);
                }
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    log.info("Crawler response: {}", content.toString());
                }
            } catch (Exception e) {
                log.error("크롤링 스크립트 실행 중 예외 발생", e);
            } finally {
                con.disconnect();
            }

        } else {
            log.info("이번 주 메뉴 데이터 존재");
        }
    }
}