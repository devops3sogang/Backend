package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Like;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface LikeRepository extends MongoRepository<Like, String> {
    // 사용자와 리뷰 ID로 좋아요 정보를 찾는 메서드
    Optional<Like> findByUserIdAndReviewId(String userId, String reviewId);

    // 사용자와 리뷰 ID로 좋아요 정보를 삭제하는 메서드
    void deleteByUserIdAndReviewId(String userId, String reviewId);
}