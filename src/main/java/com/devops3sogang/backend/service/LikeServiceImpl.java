package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Like;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public boolean toggleLike(String userId, String reviewId) {
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        // 1. MongoTemplate으로 직접 쿼리를 만들어 '좋아요'가 있는지 확인합니다.
        Query query = new Query(Criteria.where("userId").is(userId).and("reviewId").is(reviewId));
        Like existingLike = mongoTemplate.findOne(query, Like.class);

        if (existingLike != null) {
            // --- 좋아요가 이미 존재할 경우: 좋아요 취소 ---
            // 2. MongoTemplate으로 직접 '좋아요' 데이터를 삭제합니다.
            mongoTemplate.remove(existingLike);
            updateLikeCount(reviewId, -1); // likeCount 1 감소
            return false;
        } else {
            // --- 좋아요가 없을 경우: 좋아요 추가 ---
            // 3. '좋아요' 데이터를 생성하고 저장합니다. (이 부분은 Repository를 사용해도 안전합니다.)
            Like newLike = new Like(userId, reviewId);
            likeRepository.save(newLike);
            updateLikeCount(reviewId, 1); // likeCount 1 증가
            return true;
        }
    }

    private void updateLikeCount(String reviewId, int amount) {
        Query query = new Query(Criteria.where("_id").is(reviewId));
        Update update = new Update().inc("likeCount", amount);
        mongoTemplate.updateFirst(query, update, Review.class);
    }
}