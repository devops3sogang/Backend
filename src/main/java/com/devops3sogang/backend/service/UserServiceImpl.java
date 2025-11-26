package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Like;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.UserProfileResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;
import com.devops3sogang.backend.dto.UserUpdateResponse;
import com.devops3sogang.backend.exception.UserNotFoundException;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import com.devops3sogang.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getComprehensiveUserProfile(String email) {
        log.info("프로필 조회 시작 - email: {}", email);
        
        // 1. 사용자 정보를 찾습니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - email: {}", email);
                    return new UserNotFoundException(email);
                });

        // 2. 사용자가 작성한 리뷰 목록을 찾습니다.
        List<Review> writtenReviews = reviewRepository.findByUserId(user.getId());
        log.debug("작성한 리뷰 수: {}", writtenReviews.size());

        // 3. 사용자가 '좋아요'를 누른 리뷰 목록을 찾습니다.
        List<Like> userLikes = likeRepository.findByUserId(user.getId());
        List<String> likedReviewIds = userLikes.stream()
                .map(Like::getReviewId)
                .collect(Collectors.toList());

        List<Review> likedReviews = new ArrayList<>();
        if (!likedReviewIds.isEmpty()) {
            likedReviews = reviewRepository.findAllById(likedReviewIds);
        }
        log.debug("좋아요한 리뷰 수: {}", likedReviews.size());

        // 4. 모든 정보를 UserProfileResponse DTO에 담아 반환합니다.
        log.info("프로필 조회 완료 - email: {}", email);
        return UserProfileResponse.from(user, writtenReviews, likedReviews);
    }

    @Override
    @Transactional
    public User updateUserProfile(String email, UserUpdateRequest request) {
        log.info("프로필 수정 시작 - email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - email: {}", email);
                    return new UserNotFoundException(email);
                });

        // 닉네임 변경 요청이 있을 경우
        if (StringUtils.hasText(request.getNickname())) {
            String newNickname = request.getNickname();
            log.info("닉네임 변경: {} → {}", user.getNickname(), newNickname);
            user.setNickname(newNickname);

            // 1. 닉네임 변경 시, 해당 사용자가 작성한 모든 리뷰를 찾습니다.
            List<Review> reviewsWrittenByUser = reviewRepository.findByUserId(user.getId());

            // 2. 각 리뷰의 닉네임을 새로운 닉네임으로 업데이트합니다.
            for (Review review : reviewsWrittenByUser) {
                review.setNickname(newNickname);
            }

            // 3. 업데이트된 리뷰들을 한 번에 저장합니다.
            if (!reviewsWrittenByUser.isEmpty()) {
                reviewRepository.saveAll(reviewsWrittenByUser);
                log.debug("리뷰 닉네임 업데이트 완료 - {} 개", reviewsWrittenByUser.size());
            }
        }

        // 비밀번호 변경 요청이 있을 경우
        if (StringUtils.hasText(request.getPassword())) {
        if (!StringUtils.hasText(request.getOldPassword()) || 
            !passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            log.warn("비밀번호 불일치 - email: {}", email);
            throw new BadCredentialsException("현재 비밀번호가 올바르지 않습니다.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        log.info("비밀번호 변경 완료 - email: {}", email);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("프로필 수정 완료 - email: {}", email);

        return user;
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        log.info("회원 탈퇴 시작 - email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - email: {}", email);
                    return new UserNotFoundException(email);
                });

        String userId = user.getId();

        // 1. 사용자가 작성한 모든 리뷰를 찾아서 작성자 정보를 변경합니다.
        List<Review> reviewsWrittenByUser = reviewRepository.findByUserId(userId);
        for (Review review : reviewsWrittenByUser) {
            review.setNickname("(탈퇴한 회원)");
            review.setUserId(null);
        }

        // 2. 불필요한 DB 호출을 막기 위한 if문
        if (!reviewsWrittenByUser.isEmpty()) {
            reviewRepository.saveAll(reviewsWrittenByUser);
            log.debug("리뷰 작성자 정보 변경 완료 - {} 개", reviewsWrittenByUser.size());
        }

        // 3. 사용자가 눌렀던 모든 '좋아요' 기록을 삭제합니다.
        likeRepository.deleteByUserId(userId);
        log.debug("좋아요 기록 삭제 완료");

        // 4. 마지막으로 사용자 정보를 삭제합니다.
        userRepository.delete(user);
        log.info("회원 탈퇴 완료 - email: {}", email);
    }
}