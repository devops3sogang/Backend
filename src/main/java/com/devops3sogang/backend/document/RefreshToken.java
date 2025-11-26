package com.devops3sogang.backend.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    private String email;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private boolean revoked = false;
}