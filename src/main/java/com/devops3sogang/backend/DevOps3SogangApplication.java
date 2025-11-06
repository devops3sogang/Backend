package com.devops3sogang.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(servers = {
    @Server(url = "/api", description = "Base URL")
})

@SpringBootApplication
@EnableMongoAuditing
public class DevOps3SogangApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevOps3SogangApplication.class, args);
	}

}
