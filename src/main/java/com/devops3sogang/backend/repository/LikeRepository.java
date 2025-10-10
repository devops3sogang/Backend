package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Like;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 문제가 되는 메서드들을 모두 삭제하고, 기본적인 기능만 남깁니다.
public interface LikeRepository extends MongoRepository<Like, String> {
    // 사용자 ID로 좋아요 정보를 삭제하는 메서드
    void deleteByUserId(@Param("userId") String userId);

    // 특정 사용자의 모든 '좋아요'를 찾는 메서드를 추가합니다.
    List<Like> findByUserId(String userId);

    // 리뷰 ID로 좋아요 정보를 삭제하는 메서드
    void deleteByReviewId(String reviewId);
}
