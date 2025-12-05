package com.devops3sogang.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@OpenAPIDefinition(servers = {
    @Server(url = "/api", description = "Base URL")
})

@SpringBootApplication
@EnableMongoAuditing
public class DevOps3SogangApplication {

	public static void main(String[] args) {
		// .env 파일 자동 로드
		loadEnvFile();

		SpringApplication.run(DevOps3SogangApplication.class, args);
	}

	private static void loadEnvFile() {
		Path envPath = Paths.get(".env");

		if (!Files.exists(envPath)) {
			System.out.println("⚠️  .env 파일이 없습니다.");
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(envPath.toFile()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				// 주석과 빈 줄 무시
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}

				// key=value 형식 파싱
				String[] parts = line.split("=", 2);
				if (parts.length == 2) {
					String key = parts[0].trim();
					String value = parts[1].trim();

					// 환경 변수가 아직 설정되지 않은 경우에만 설정
					if (System.getenv(key) == null) {
						System.setProperty(key, value);
					}
				}
			}
			System.out.println("✅ .env 파일 로드 완료");
		} catch (IOException e) {
			System.err.println("⚠️  .env 파일 읽기 실패: " + e.getMessage());
		}
	}

}
