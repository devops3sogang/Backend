package com.devops3sogang.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // 1. SecurityScheme 정의: JWT 인증 방식을 설명합니다.
        final String securitySchemeName = "Bearer Authentication";
        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP) // 인증 타입: HTTP
                .scheme("bearer") // 스킴: Bearer
                .bearerFormat("JWT"); // 베어러 포맷: JWT

        // 2. SecurityRequirement 정의: 위에서 정의한 SecurityScheme을 전역적으로 적용합니다.
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);

        // 3. OpenAPI 객체에 반영
        return new OpenAPI()
                .info(new Info()
                        .title("MongoSpring REST API")
                        .description("Spring Boot + MongoDB 기반 REST API 문서입니다.")
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 서버")
                ))
                // Components에 SecurityScheme 추가
                .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme))
                // SecurityRequirement에 전역 설정 추가
                .addSecurityItem(securityRequirement);
    }
}