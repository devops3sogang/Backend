package com.devops3sogang.backend.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "likes")
public class Like {
    @Id
    private String id;

    private String userId;   // 좋아요를 누른 사용자 ID
    private String reviewId; // 좋아요가 눌린 리뷰 ID

    private LocalDateTime createdAt;

    public Like(String userId, String reviewId) {
        this.userId = userId;
        this.reviewId = reviewId;
        this.createdAt = LocalDateTime.now();
    }
}