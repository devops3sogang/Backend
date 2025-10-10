package com.devops3sogang.backend.config;

import com.devops3sogang.backend.config.jwt.JwtAuthenticationFilter;
import com.devops3sogang.backend.config.jwt.JwtUtil;
import com.devops3sogang.backend.config.security.CustomAccessDeniedHandler;
import com.devops3sogang.backend.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

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

                // 예외 처리(Exception Handling) 설정을 추가합니다.
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                .authorizeHttpRequests(authorize -> authorize
                        // --- 인증 불필요 경로 ---
                        .requestMatchers("/auth/**", "/restaurants-view").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**").permitAll()
                        // GET 요청은 대부분 허용
                        .requestMatchers(HttpMethod.GET).permitAll()

                        // '/admin/'으로 시작하는 모든 경로는 'ADMIN' 역할이 필요
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 맛집 등록은 ADMIN만
                        .requestMatchers(HttpMethod.POST, "/restaurants").hasRole("ADMIN")

                        // --- 인증 필요 경로 (구체적으로 명시) ---
                        .requestMatchers("/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/restaurants/{restaurantId}/reviews").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/reviews/{reviewId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/reviews/{reviewId}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/reviews/{reviewId}/like").authenticated()

                        // 그 외 모든 요청은 거부
                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}