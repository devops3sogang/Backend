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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public boolean toggleLike(String userId, String reviewId) {
        // 리뷰가 존재하는지 먼저 확인
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        Optional<Like> likeOptional = likeRepository.findByUserIdAndReviewId(userId, reviewId);

        if (likeOptional.isPresent()) {
            // --- 좋아요가 이미 존재할 경우: 좋아요 취소 ---
            likeRepository.delete(likeOptional.get());
            updateLikeCount(reviewId, -1); // likeCount 1 감소
            return false; // '좋아요' 상태 아님
        } else {
            // --- 좋아요가 없을 경우: 좋아요 추가 ---
            Like newLike = new Like(userId, reviewId);
            likeRepository.save(newLike);
            updateLikeCount(reviewId, 1); // likeCount 1 증가
            return true; // '좋아요' 상태임
        }
    }

    private void updateLikeCount(String reviewId, int amount) {
        Query query = new Query(Criteria.where("_id").is(reviewId));
        Update update = new Update().inc("likeCount", amount);
        mongoTemplate.updateFirst(query, update, Review.class);
    }
}