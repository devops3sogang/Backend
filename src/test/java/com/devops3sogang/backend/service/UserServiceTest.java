package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Rating;
import com.devops3sogang.backend.document.Like;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.UserProfileResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;
import com.devops3sogang.backend.exception.UserNotFoundException;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import com.devops3sogang.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private List<Review> writtenReviews;
    private List<Like> userLikes;
    private List<Review> likedReviews;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 11, 29, 0, 0);
        // 테스트 유저
        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");
        testUser.setNickname("테스터");
        testUser.setPasswordHash("encodedPassword");
        testUser.setCreatedAt(testDateTime);
        testUser.setUpdatedAt(testDateTime);

        // 작성한 리뷰 목록
        Review review1 = new Review();
        review1.setId("review1");
        review1.setUserId("user123");
        review1.setNickname("테스터");
        review1.setContent("좋은 제품입니다");
        Rating rating1 = new Rating();
        Rating.MenuRating menuRating1 = new Rating.MenuRating();
        menuRating1.setMenuId("menu1");
        menuRating1.setRating(5);
        rating1.setMenuRatings(List.of(menuRating1));
        rating1.setRestaurantRating(5);
        review1.setRating(rating1);

        Review review2 = new Review();
        review2.setId("review2");
        review2.setUserId("user123");
        review2.setNickname("테스터");
        review2.setContent("추천합니다");
        Rating rating2 = new Rating();
        Rating.MenuRating menuRating2 = new Rating.MenuRating();
        menuRating2.setMenuId("menu2");
        menuRating2.setRating(4);
        rating2.setMenuRatings(List.of(menuRating1));
        rating2.setRestaurantRating(4);
        review2.setRating(rating2);

        writtenReviews = Arrays.asList(review1, review2);

        // 좋아요 목록
        Like like1 = new Like();
        like1.setId("like1");
        like1.setUserId("user123");
        like1.setReviewId("review3");

        Like like2 = new Like();
        like2.setId("like2");
        like2.setUserId("user123");
        like2.setReviewId("review4");

        userLikes = Arrays.asList(like1, like2);

        // 좋아요 누른 리뷰 목록
        Review likedReview1 = new Review();
        likedReview1.setId("review3");
        likedReview1.setUserId("otherUser");
        likedReview1.setContent("유용한 리뷰");
        Rating rating3 = new Rating();
        Rating.MenuRating menuRating3 = new Rating.MenuRating();
        menuRating3.setMenuId("menu1");
        menuRating3.setRating(3);
        rating3.setMenuRatings(List.of(menuRating3));
        rating3.setRestaurantRating(4);
        likedReview1.setRating(rating3);

        Review likedReview2 = new Review();
        likedReview2.setId("review4");
        likedReview2.setUserId("otherUser2");
        likedReview2.setContent("도움됐어요");
        Rating rating4 = new Rating();
        Rating.MenuRating menuRating4 = new Rating.MenuRating();
        menuRating4.setMenuId("menu1");
        menuRating4.setRating(4);
        rating4.setMenuRatings(List.of(menuRating4));
        rating4.setRestaurantRating(4);
        likedReview1.setRating(rating4);

        likedReviews = Arrays.asList(likedReview1, likedReview2);
    }

    @Test
    @DisplayName("통합 프로필 조회 성공")
    void getComprehensiveUserProfile_Success() {
        // given
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(reviewRepository.findByUserId("user123")).willReturn(writtenReviews);
        given(likeRepository.findByUserId("user123")).willReturn(userLikes);
        given(reviewRepository.findAllById(Arrays.asList("review3", "review4"))).willReturn(likedReviews);

        // when
        UserProfileResponse response = userService.getComprehensiveUserProfile("test@example.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("user123");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("테스터");
        assertThat(response.getWrittenReviews()).hasSize(2);
        assertThat(response.getLikedReviews()).hasSize(2);

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(reviewRepository, times(1)).findByUserId("user123");
        verify(likeRepository, times(1)).findByUserId("user123");
        verify(reviewRepository, times(1)).findAllById(anyList());
    }

    @Test
    @DisplayName("통합 프로필 조회 성공 - 좋아요한 리뷰 없음")
    void getComprehensiveUserProfile_NoLikedReviews() {
        // given
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(reviewRepository.findByUserId("user123")).willReturn(writtenReviews);
        given(likeRepository.findByUserId("user123")).willReturn(Collections.emptyList());

        // when
        UserProfileResponse response = userService.getComprehensiveUserProfile("test@example.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getWrittenReviews()).hasSize(2);
        assertThat(response.getLikedReviews()).isEmpty();

        verify(reviewRepository, never()).findAllById(anyList());
    }

    @Test
    @DisplayName("통합 프로필 조회 실패 - 사용자 없음")
    void getComprehensiveUserProfile_UserNotFound() {
        // given
        given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getComprehensiveUserProfile("notfound@example.com"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findByEmail("notfound@example.com");
        verify(reviewRepository, never()).findByUserId(anyString());
        verify(likeRepository, never()).findByUserId(anyString());
    }

    @Test
    @DisplayName("프로필 수정 성공 - 닉네임만 변경")
    void updateUserProfile_NicknameOnly() {
        // given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("새닉네임");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(reviewRepository.findByUserId("user123")).willReturn(writtenReviews);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        User result = userService.updateUserProfile("test@example.com", request);

        // then
        assertThat(result.getNickname()).isEqualTo("새닉네임");

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(reviewRepository, times(1)).findByUserId("user123");
        verify(reviewRepository, times(1)).saveAll(anyList());
        verify(userRepository, times(1)).save(any(User.class));

        // 리뷰의 닉네임도 변경되었는지 확인
        ArgumentCaptor<List<Review>> reviewCaptor = ArgumentCaptor.forClass(List.class);
        verify(reviewRepository).saveAll(reviewCaptor.capture());
        List<Review> savedReviews = reviewCaptor.getValue();
        assertThat(savedReviews).allMatch(review -> review.getNickname().equals("새닉네임"));
    }

    @Test
    @DisplayName("프로필 수정 성공 - 비밀번호만 변경")
    void updateUserProfile_PasswordOnly() {
        // given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setCurrentPassword("oldPassword");
        request.setPassword("newPassword123!");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("oldPassword", "encodedPassword")).willReturn(true);
        given(passwordEncoder.encode("newPassword123!")).willReturn("newEncodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        User result = userService.updateUserProfile("test@example.com", request);

        // then
        verify(passwordEncoder, times(1)).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder, times(1)).encode("newPassword123!");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("프로필 수정 성공 - 닉네임과 비밀번호 동시 변경")
    void updateUserProfile_NicknameAndPassword() {
        // given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("새닉네임");
        request.setCurrentPassword("oldPassword");
        request.setPassword("newPassword123!");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(reviewRepository.findByUserId("user123")).willReturn(writtenReviews);
        given(passwordEncoder.matches("oldPassword", "encodedPassword")).willReturn(true);
        given(passwordEncoder.encode("newPassword123!")).willReturn("newEncodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        User result = userService.updateUserProfile("test@example.com", request);

        // then
        assertThat(result.getNickname()).isEqualTo("새닉네임");

        verify(reviewRepository, times(1)).saveAll(anyList());
        verify(passwordEncoder, times(1)).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder, times(1)).encode("newPassword123!");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("프로필 수정 실패 - 현재 비밀번호 불일치")
    void updateUserProfile_InvalidCurrentPassword() {
        // given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setCurrentPassword("wrongPassword");
        request.setPassword("newPassword123!");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile("test@example.com", request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("현재 비밀번호가 올바르지 않습니다");

        verify(passwordEncoder, times(1)).matches("wrongPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("프로필 수정 실패 - 현재 비밀번호 미입력")
    void updateUserProfile_MissingCurrentPassword() {
        // given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPassword("newPassword123!");
        // currentPassword 없음

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile("test@example.com", request))
                .isInstanceOf(BadCredentialsException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("프로필 수정 실패 - 사용자 없음")
    void updateUserProfile_UserNotFound() {
        // given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("새닉네임");

        given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile("notfound@example.com", request))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findByEmail("notfound@example.com");
        verify(reviewRepository, never()).findByUserId(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteUser_Success() {
        // given
        String rawPassword = "password123";
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(rawPassword, "encodedPassword")).willReturn(true);
        given(reviewRepository.findByUserId("user123")).willReturn(writtenReviews);
        willDoNothing().given(likeRepository).deleteByUserId("user123");
        willDoNothing().given(userRepository).delete(testUser);

        // when
        userService.deleteUser("test@example.com", rawPassword);

        // then
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches(rawPassword, "encodedPassword");
        verify(reviewRepository, times(1)).findByUserId("user123");
        verify(reviewRepository, times(1)).saveAll(anyList());
        verify(likeRepository, times(1)).deleteByUserId("user123");
        verify(userRepository, times(1)).delete(testUser);

        // 리뷰가 "(탈퇴한 회원)"으로 변경되었는지 확인
        ArgumentCaptor<List<Review>> reviewCaptor = ArgumentCaptor.forClass(List.class);
        verify(reviewRepository).saveAll(reviewCaptor.capture());
        List<Review> savedReviews = reviewCaptor.getValue();
        assertThat(savedReviews).allMatch(review -> 
            review.getNickname().equals("(탈퇴한 회원)") && review.getUserId() == null
        );
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 작성한 리뷰 없음")
    void deleteUser_NoReviews() {
        // given
        String rawPassword = "password123";
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(rawPassword, "encodedPassword")).willReturn(true);
        given(reviewRepository.findByUserId("user123")).willReturn(Collections.emptyList());
        willDoNothing().given(likeRepository).deleteByUserId("user123");
        willDoNothing().given(userRepository).delete(testUser);

        // when
        userService.deleteUser("test@example.com", rawPassword);

        // then
        verify(reviewRepository, times(1)).findByUserId("user123");
        verify(reviewRepository, never()).saveAll(anyList());
        verify(likeRepository, times(1)).deleteByUserId("user123");
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 비밀번호 불일치")
    void deleteUser_InvalidPassword() {
        // given
        String wrongPassword = "wrongPassword";
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(wrongPassword, "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.deleteUser("test@example.com", wrongPassword))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다");

        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches(wrongPassword, "encodedPassword");
        verify(reviewRepository, never()).findByUserId(anyString());
        verify(likeRepository, never()).deleteByUserId(anyString());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 사용자 없음")
    void deleteUser_UserNotFound() {
        // given
        given(userRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteUser("notfound@example.com", "password123"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findByEmail("notfound@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(reviewRepository, never()).findByUserId(anyString());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("닉네임 변경 시 리뷰 업데이트 확인")
    void updateUserProfile_UpdatesReviewNicknames() {
        // given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("완전히새로운닉네임");

        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(reviewRepository.findByUserId("user123")).willReturn(writtenReviews);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        userService.updateUserProfile("test@example.com", request);

        // then
        ArgumentCaptor<List<Review>> captor = ArgumentCaptor.forClass(List.class);
        verify(reviewRepository).saveAll(captor.capture());
        List<Review> updatedReviews = captor.getValue();

        assertThat(updatedReviews).hasSize(2);
        assertThat(updatedReviews.get(0).getNickname()).isEqualTo("완전히새로운닉네임");
        assertThat(updatedReviews.get(1).getNickname()).isEqualTo("완전히새로운닉네임");
    }

    @Test
    @DisplayName("회원 탈퇴 시 리뷰 익명화 확인")
    void deleteUser_AnonymizesReviews() {
        // given
        String rawPassword = "password123";
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(rawPassword, "encodedPassword")).willReturn(true);
        given(reviewRepository.findByUserId("user123")).willReturn(writtenReviews);
        willDoNothing().given(likeRepository).deleteByUserId("user123");
        willDoNothing().given(userRepository).delete(testUser);

        // when
        userService.deleteUser("test@example.com", rawPassword);

        // then
        ArgumentCaptor<List<Review>> captor = ArgumentCaptor.forClass(List.class);
        verify(reviewRepository).saveAll(captor.capture());
        List<Review> anonymizedReviews = captor.getValue();

        assertThat(anonymizedReviews).hasSize(2);
        assertThat(anonymizedReviews).allMatch(review -> 
            review.getNickname().equals("(탈퇴한 회원)")
        );
        assertThat(anonymizedReviews).allMatch(review -> review.getUserId() == null);
    }
}