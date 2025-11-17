package com.devops3sogang.backend.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret; // application.properties에서 시크릿 키 주입

    private Key key; // 암호화된 Key 객체

    private final long expireMs = 1000 * 60 * 60; // 토큰 유효시간: 1시간

    // 의존성 주입 후 초기화를 수행하는 메서드
    @PostConstruct
    public void init() {
        // String 형태의 시크릿 키를 Key 객체로 변환
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰 생성
     */
    public String createToken(String email) {
        Claims claims = Jwts.claims();
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(key, SignatureAlgorithm.HS256) // String 대신 Key 객체 사용
                .compact();
    }

    /**
     * Claims에서 email 추출
     */
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    /**
     * 토큰 유효성 및 만료일자 확인
     */
    public boolean validateToken(String token) {
        try {
            // 파싱 과정에서 토큰이 유효하지 않으면 예외가 발생함
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 유효하지 않은 토큰 (만료, 변조 등)
            return false;
        }
    }

    public long getExpirationTimeMillis(String token) {
        return Jwts.parserBuilder()
               .setSigningKey(key)
               .build()
               .parseClaimsJws(token)
               .getBody()
               .getExpiration()
               .getTime();
    }
}