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
                ProcessBuilder pb = new ProcessBuilder(
                        "python3", 
                        "devops3sogang/crawling/crawler.py" //절대경로로 바꿔야 빌드됨
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                // 스크립트 실행 로그 출력
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info(line);
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