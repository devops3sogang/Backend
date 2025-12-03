package com.devops3sogang.backend.util;

import com.devops3sogang.backend.repository.OnCampusMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

        boolean menuExists = !onCampusMenuRepository.findByWeekStartDate(currentWeekStartDate).isEmpty();

        if (!menuExists) {
            log.info("이번 주 메뉴 데이터가 DB에 없습니다. 초기 데이터 크롤링을 실행합니다.");

            // Python 스크립트 실행
            try {
                // 프로젝트 루트 기준 상대 경로
                String scriptPath = System.getProperty("user.dir") + "/../crawling/crawler.py";
                ProcessBuilder pb = new ProcessBuilder(
                        "python",
                        scriptPath
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                // Python 출력 스트림을 소비만 하고 로그에 출력하지 않음 (한글 깨짐 방지)
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    while (reader.readLine() != null) {
                        // 출력을 읽어서 버퍼를 비우지만 로그에는 출력하지 않음
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    log.info("크롤링 스크립트가 성공적으로 완료되었습니다.");
                } else {
                    log.error("크롤링 스크립트 실행 중 오류가 발생했습니다. 종료 코드: {}", exitCode);
                }

            } catch (Exception e) {
                log.error("크롤링 스크립트 실행 중 예외 발생", e);
            }

        } else {
            log.info("이번 주 메뉴 데이터가 이미 DB에 존재하므로, 초기 크롤링을 건너뜁니다.");
        }
    }
}