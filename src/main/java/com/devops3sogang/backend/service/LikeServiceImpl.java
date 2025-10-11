package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Like;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.exception.ReviewNotFoundException;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public boolean toggleLike(String userId, String reviewId) {
        log.info("좋아요 토글 시작 - userId: {}, reviewId: {}", userId, reviewId);
        
        // 리뷰 존재 확인
        reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("리뷰를 찾을 수 없음 - reviewId: {}", reviewId);
                    return new ReviewNotFoundException(reviewId);
                });
        
        // 좋아요 존재 확인 (Repository 메서드 사용 - 훨씬 간단!)
        Optional<Like> existingLike = likeRepository.findByUserIdAndReviewId(userId, reviewId);
        
        if (existingLike.isPresent()) {
            // 좋아요 취소
            log.info("좋아요 취소 - userId: {}, reviewId: {}", userId, reviewId);
            likeRepository.delete(existingLike.get());
            updateLikeCount(reviewId, -1);
            log.debug("좋아요 개수 감소 완료 - reviewId: {}", reviewId);
            return false;
        } else {
            // 좋아요 추가
            log.info("좋아요 추가 - userId: {}, reviewId: {}", userId, reviewId);
            Like newLike = new Like(userId, reviewId);
            likeRepository.save(newLike);
            updateLikeCount(reviewId, 1);
            log.debug("좋아요 개수 증가 완료 - reviewId: {}", reviewId);
            return true;
        }
    }

    private void updateLikeCount(String reviewId, int amount) {
        log.debug("좋아요 개수 업데이트 - reviewId: {}, amount: {}", reviewId, amount);
        Query query = new Query(Criteria.where("_id").is(reviewId));
        Update update = new Update().inc("likeCount", amount);
        mongoTemplate.updateFirst(query, update, Review.class);
    }
}