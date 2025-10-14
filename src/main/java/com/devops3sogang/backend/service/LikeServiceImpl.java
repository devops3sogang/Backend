package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Like;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.dto.CreateLikeResponse;
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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Override
    @Transactional
    public CreateLikeResponse toggleLike(String userId, String reviewId) {
        log.info("좋아요 토글 시작 - userId: {}, reviewId: {}", userId, reviewId);

        // 좋아요 존재 확인
        Optional<Like> existingLike = likeRepository.findByUserIdAndReviewId(userId, reviewId);

        if (existingLike.isPresent()) {
            // 좋아요 취소
            log.info("좋아요 취소 - userId: {}, reviewId: {}", userId, reviewId);
            Like like = existingLike.get();
            likeRepository.delete(like);
            int updatedCount = updateLikeCount(reviewId, -1);
            log.debug("좋아요 개수 감소 완료 - reviewId: {}, 현재 개수: {}", reviewId, updatedCount);

            // 삭제된 좋아요 정보로 응답 생성
            CreateLikeResponse.LikeDto deletedLikeDto = new CreateLikeResponse.LikeDto(
                like.getId(),
                like.getUserId(),
                like.getReviewId(),
                like.getCreatedAt().atZone(ZoneId.systemDefault()).format(ISO_FORMATTER)
            );
            return CreateLikeResponse.forDeletion(deletedLikeDto, updatedCount);
        } else {
            // 좋아요 추가
            log.info("좋아요 추가 - userId: {}, reviewId: {}", userId, reviewId);
            Like newLike = new Like(userId, reviewId);
            Like savedLike = likeRepository.save(newLike);
            int updatedCount = updateLikeCount(reviewId, 1);
            log.debug("좋아요 개수 증가 완료 - reviewId: {}, 현재 개수: {}", reviewId, updatedCount);

            // 생성된 좋아요 정보로 응답 생성
            CreateLikeResponse.LikeDto createdLikeDto = new CreateLikeResponse.LikeDto(
                savedLike.getId(),
                savedLike.getUserId(),
                savedLike.getReviewId(),
                savedLike.getCreatedAt().atZone(ZoneId.systemDefault()).format(ISO_FORMATTER)
            );
            return new CreateLikeResponse(createdLikeDto, updatedCount);
        }
    }

    private int updateLikeCount(String reviewId, int amount) {
        log.debug("좋아요 개수 업데이트 - reviewId: {}, amount: {}", reviewId, amount);
        Query query = new Query(Criteria.where("_id").is(reviewId));
        Update update = new Update().inc("likeCount", amount);
        mongoTemplate.updateFirst(query, update, Review.class);

        // 업데이트된 리뷰 조회하여 현재 likeCount 반환
        Review updatedReview = reviewRepository.findById(reviewId).orElseThrow();
        return updatedReview.getLikeCount();
    }
}