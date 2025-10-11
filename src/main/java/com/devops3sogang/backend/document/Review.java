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
    
    @CreatedDate  // ← 자동으로 설정됨
    private LocalDateTime createdAt;
    
    @LastModifiedDate  // ← 자동으로 업데이트됨
    private LocalDateTime updatedAt;
}