package com.devops3sogang.backend.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String userId;
    private String nickname;
    private ReviewTarget target;
    private Ratings ratings;
    private String content;
    private String imageUrl;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}