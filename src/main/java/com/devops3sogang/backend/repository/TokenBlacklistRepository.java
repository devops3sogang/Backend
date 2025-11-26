package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.TokenBlacklist;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TokenBlacklistRepository extends MongoRepository<TokenBlacklist, String> {

    Optional<TokenBlacklist> findByToken(String token);

    void deleteByToken(String token);
}