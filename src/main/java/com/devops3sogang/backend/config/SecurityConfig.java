package com.devops3sogang.backend.config;

import com.devops3sogang.backend.config.jwt.JwtAuthenticationFilter;
import com.devops3sogang.backend.config.jwt.JwtUtil;
import com.devops3sogang.backend.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // HttpMethod import 추가
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // 인증/인가 API 경로 모두 허용
                        .requestMatchers("/api/auth/**").permitAll()
                        // Swagger UI 관련 경로 허용
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll()
                        // 웹페이지 뷰(View) 경로 모두 허용
                        .requestMatchers("/restaurants-view").permitAll()
                        // GET 요청에 대한 경로 모두 허용
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/**", "/api/on-campus-menus/**").permitAll()

                        // POST 요청에 대한 경로들을 명시적으로 인증만 필요하다고 설정
                        .requestMatchers(HttpMethod.POST, "/api/restaurants/**", "/api/reviews/**").authenticated()

                        // 위에서 지정하지 않은 그 외 모든 요청은 일단 거부 (더 안전한 방식)
                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}