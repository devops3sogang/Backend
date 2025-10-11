package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Like;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends MongoRepository<Like, String> {
    // 좋아요 토글용
    Optional<Like> findByUserIdAndReviewId(String userId, String reviewId);
    
    // 사용자의 모든 좋아요
    List<Like> findByUserId(String userId);
    
    // 리뷰의 모든 좋아요
    List<Like> findByReviewId(String reviewId);
    
    // 사용자 삭제 시
    void deleteByUserId(String userId);
    
    // 리뷰 삭제 시
    void deleteByReviewId(String reviewId);
    
    // 식당 삭제 시 (여러 리뷰의 좋아요 삭제)
    void deleteAllByReviewIdIn(List<String> reviewIds);
}