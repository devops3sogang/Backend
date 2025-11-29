package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.RefreshToken;
import com.devops3sogang.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateRefreshToken(String email) {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        String plainToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = hashToken(plainToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .email(email)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().plusDays(14))
                .revoked(false)
                .build();

        repository.save(refreshToken);
        return plainToken; // 클라이언트에게 전달할 토큰
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public RefreshToken validateRefreshToken(String plainToken) {
        String hash = hashToken(plainToken);
        return repository.findAll().stream()
                .filter(rt -> !rt.isRevoked() && rt.getTokenHash().equals(hash))
                .findFirst()
                .orElse(null);
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        repository.save(token);
    }

    public void invalidate(String email) {
        repository.findAll().stream()
            .filter(rt -> rt.getEmail().equals(email) && !rt.isRevoked())
            .forEach(rt -> {
                rt.setRevoked(true);
                repository.save(rt);
            });
    }
}