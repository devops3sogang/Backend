package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteAllByEmail(String email);
}