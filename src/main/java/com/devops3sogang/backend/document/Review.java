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
    private String nickname; //작성자닉네임? 
    private ReviewTarget target; //리뷰대상 식당?메뉴? 
    private Ratings ratings;
    private String content;
    private String imageUrl;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}