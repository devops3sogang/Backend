package com.devops3sogang.backend.config;

import com.devops3sogang.backend.config.jwt.JwtAuthenticationFilter;
import com.devops3sogang.backend.config.jwt.JwtUtil;
import com.devops3sogang.backend.config.security.CustomAccessDeniedHandler;
import com.devops3sogang.backend.config.security.CustomAuthenticationEntryPoint;
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final JwtUtil jwtUtil;
        private final UserDetailsServiceImpl userDetailsService;
        private final CustomAccessDeniedHandler customAccessDeniedHandler;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        // --- 1. 인증 불필요 경로 (가장 먼저) ---
                        .requestMatchers("/auth/**", "/restaurants-view", "/on-campus-menus", "/on-campus-menus/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                        "/swagger-resources/**",
                                        "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/uploads/**").permitAll() // 업로드된 이미지 접근 허용

                        // --- 2. 관리자 전용 경로 ---
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/restaurants").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/restaurants/**").hasRole("ADMIN")

                        // --- 3. 인증 필요 경로 (구체적으로 명시) ---
                        .requestMatchers("/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/images/upload").authenticated() // 이미지 업로드는 인증 필요
                        .requestMatchers(HttpMethod.POST, "/restaurants/{restaurantId}/reviews")
                        .authenticated()
                        .requestMatchers(HttpMethod.PUT, "/reviews/{reviewId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/reviews/{reviewId}")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/reviews/{reviewId}/like")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/reviews").authenticated()

                        // --- 4. GET 요청은 대부분 허용 (마지막에) ---
                        .requestMatchers(HttpMethod.GET).permitAll()

                        // --- 5. 그 외 모든 요청은 거부 ---
                        .anyRequest().denyAll())
                .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService),
                                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "https://43.202.229.52", "https://xn--939at21b.xn--299aj40a3hj8tm.xn--h32bi4v.xn--3e0b707e")); 
            configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("*"));
            configuration.setAllowCredentials(false);
            configuration.setMaxAge(3600L);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
    }
}