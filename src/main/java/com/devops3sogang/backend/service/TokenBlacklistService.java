package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.TokenBlacklist;
import com.devops3sogang.backend.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository repository;

    // 로그아웃 시 토큰 등록
    public void blacklistToken(String token, LocalDateTime expiresAt) {
        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();
        repository.save(blacklist);
    }

    // 인증 필터에서 검증
    public boolean isBlacklisted(String token) {
        return repository.findByToken(token).isPresent();
    }

    // 수동 토큰 제거
    public void remove(String token) {
        repository.deleteByToken(token);
    }
}