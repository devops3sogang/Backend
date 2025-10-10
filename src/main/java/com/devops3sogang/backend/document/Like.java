package com.devops3sogang.backend.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor // 1. 인자 없는 기본 생성자 자동 생성
@Document(collection = "likes")
public class Like {
    @Id
    private String id;

    private String userId;
    private String reviewId;

    private LocalDateTime createdAt;

    // 2. 서비스에서 사용할 생성자는 그대로 둡니다.
    public Like(String userId, String reviewId) {
        this.userId = userId;
        this.reviewId = reviewId;
        this.createdAt = LocalDateTime.now();
    }

    // 3. 모든 필드를 받는 생성자를 추가할 수도 있습니다.
    public Like(String id, String userId, String reviewId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.reviewId = reviewId;
        this.createdAt = createdAt;
    }
}