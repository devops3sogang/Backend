package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Like;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.UserProfileResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import com.devops3sogang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getComprehensiveUserProfile(String email) {
        // 1. 사용자 정보를 찾습니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 사용자가 작성한 리뷰 목록을 찾습니다.
        List<Review> writtenReviews = reviewRepository.findByUserId(user.getId());

        // 3. 사용자가 '좋아요'를 누른 리뷰 목록을 찾습니다. (기존 getLikedReviews 로직)
        List<Like> userLikes = likeRepository.findByUserId(user.getId());
        List<String> likedReviewIds = userLikes.stream()
                .map(Like::getReviewId)
                .collect(Collectors.toList());

        List<Review> likedReviews = new ArrayList<>();
        if (!likedReviewIds.isEmpty()) {
            likedReviews = reviewRepository.findAllById(likedReviewIds);
        }

        // 4. 모든 정보를 UserProfileResponse DTO에 담아 반환합니다.
        return UserProfileResponse.from(user, writtenReviews, likedReviews);
    }

    @Override
    public void updateUserProfile(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 닉네임 변경 요청이 있을 경우
        if (StringUtils.hasText(request.getNickname())) {
            String newNickname = request.getNickname();
            user.setNickname(request.getNickname());

            // 1. 닉네임 변경 시, 해당 사용자가 작성한 모든 리뷰를 찾습니다.
            List<Review> reviewsWrittenByUser = reviewRepository.findByUserId(user.getId());

            // 2. 각 리뷰의 닉네임을 새로운 닉네임으로 업데이트합니다.
            for (Review review : reviewsWrittenByUser) {
                review.setNickname(newNickname);
            }

            // 3. 업데이트된 리뷰들을 한 번에 저장합니다.
            if (!reviewsWrittenByUser.isEmpty()) {
                reviewRepository.saveAll(reviewsWrittenByUser);
            }
        }

        // 비밀번호 변경 요청이 있을 경우
        if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
    }

    @Override
    @Transactional // 여러 DB 작업을 하나의 트랜잭션으로 묶어 안정성 확보
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String userId = user.getId();

        // 1. 사용자가 작성한 모든 리뷰를 찾아서 작성자 정보를 변경합니다.
        List<Review> reviewsWrittenByUser = reviewRepository.findByUserId(userId);
        for (Review review : reviewsWrittenByUser) {
            review.setNickname("(알 수 없음)");
            // userId는 null로 만들거나 특정 값으로 설정할 수 있습니다.
            review.setUserId(null);
        }
        reviewRepository.saveAll(reviewsWrittenByUser);

        // 2. 사용자가 눌렀던 모든 '좋아요' 기록을 삭제합니다.
        likeRepository.deleteByUserId(userId);

        // 3. 마지막으로 사용자 정보를 삭제합니다.
        userRepository.delete(user);
    }
}