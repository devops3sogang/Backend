package com.devops3sogang.backend.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "likes")
public class Like {
    @Id
    private String id;
    private String userId;
    private String reviewId;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    public Like(String userId, String reviewId) {
        this.userId = userId;
        this.reviewId = reviewId;
    }
}