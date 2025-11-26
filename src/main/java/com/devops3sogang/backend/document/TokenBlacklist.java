package com.devops3sogang.backend.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "token_blacklist")
public class TokenBlacklist {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;           // AccessToken

    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime expiresAt;
}