package com.devops3sogang.backend.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String userId;
    private String nickname;
    private ReviewTarget target;
    private Rating rating;
    private String content;
    private List<String> imageUrls;
    private int likeCount;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}