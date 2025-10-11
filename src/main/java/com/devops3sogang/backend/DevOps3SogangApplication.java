package com.devops3sogang.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMongoAuditing
public class DevOps3SogangApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevOps3SogangApplication.class, args);
	}

}
