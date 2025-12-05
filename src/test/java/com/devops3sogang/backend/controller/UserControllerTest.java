package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Rating;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.DeleteUserRequest;
import com.devops3sogang.backend.dto.UserProfileResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;
import com.devops3sogang.backend.dto.UserUpdateResponse;
import com.devops3sogang.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean(name = "mongoAuditingHandler")
    private Object mongoAuditingHandler;

    private User testUser;
    private UserProfileResponse userProfileResponse;
    private UserUpdateResponse userUpdateResponse;
    private List<Review> writtenReviews;
    private List<Review> likedReviews;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 11, 29, 0, 0);

        // 테스트 유저
        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@test.com");
        testUser.setNickname("테스터");
        testUser.setPasswordHash("encodedPassword");
        testUser.setCreatedAt(testDateTime);
        testUser.setUpdatedAt(testDateTime);

        // 작성한 리뷰 목록
        Review review1 = new Review();
        review1.setId("review1");
        review1.setUserId("user123");
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
        review2.setContent("추천합니다");
        Rating rating2 = new Rating();
        Rating.MenuRating menuRating2 = new Rating.MenuRating();
        menuRating2.setMenuId("menu2");
        menuRating2.setRating(4);
        rating2.setMenuRatings(List.of(menuRating2));
        rating2.setRestaurantRating(4);
        review2.setRating(rating2);

        writtenReviews = Arrays.asList(review1, review2);

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

        likedReviews = Arrays.asList(likedReview1);

        // UserProfileResponse 생성
        userProfileResponse = UserProfileResponse.builder()
                .id("user123")
                .email("test@test.com")
                .nickname("테스터")
                .role(Role.USER)
                .createdAt(testDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .writtenReviews(writtenReviews)
                .likedReviews(likedReviews)
                .build();

        // UserUpdateResponse 생성
        userUpdateResponse = UserUpdateResponse.builder()
                .id("user123")
                .email("test@test.com")
                .nickname("업데이트됨")
                .updatedAt(testDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    @Test
    @DisplayName("GET /users/me - 사용자 프로필 조회 성공")
    void getMyProfile_Success() throws Exception {
        // given
        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("test@test.com");
        given(userService.getComprehensiveUserProfile("test@test.com"))
                .willReturn(userProfileResponse);

        // when & then
        mockMvc.perform(get("/users/me")
                        .principal(auth))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value("user123"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.myReviews").isArray())
                .andExpect(jsonPath("$.myReviews.length()").value(2))
                .andExpect(jsonPath("$.myReviews[0].id").value("review1"))
                .andExpect(jsonPath("$.myReviews[0].content").value("좋은 제품입니다"))
                .andExpect(jsonPath("$.myReviews[0].rating.menuRatings[0].rating").value(5))
                .andExpect(jsonPath("$.myReviews[1].id").value("review2"))
                .andExpect(jsonPath("$.likedReviews").isArray())
                .andExpect(jsonPath("$.likedReviews.length()").value(1))
                .andExpect(jsonPath("$.likedReviews[0].id").value("review3"));

        verify(userService, times(1)).getComprehensiveUserProfile("test@test.com");
    }

    @Test
    @DisplayName("GET /users/me - 작성한 리뷰가 없는 경우")
    void getMyProfile_NoWrittenReviews() throws Exception {
        // given
        UserProfileResponse emptyReviewsResponse = UserProfileResponse.builder()
                .id("user123")
                .email("test@test.com")
                .nickname("테스터")
                .role(Role.USER)
                .createdAt(testDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .writtenReviews(new ArrayList<>())
                .likedReviews(new ArrayList<>())
                .build();

        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("test@test.com");
        given(userService.getComprehensiveUserProfile("test@test.com"))
                .willReturn(emptyReviewsResponse);

        // when & then
        mockMvc.perform(get("/users/me")
                        .principal(auth))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value("user123"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.myReviews").isArray())
                .andExpect(jsonPath("$.myReviews.length()").value(0))
                .andExpect(jsonPath("$.likedReviews").isArray())
                .andExpect(jsonPath("$.likedReviews.length()").value(0));

        verify(userService, times(1)).getComprehensiveUserProfile("test@test.com");
    }

    @Test
    @DisplayName("GET /users/me - ADMIN 권한 사용자 프로필 조회")
    void getMyProfile_AdminUser() throws Exception {
        // given
        Authentication adminAuth = mock(Authentication.class);
        given(adminAuth.getName()).willReturn("admin@test.com");

        UserProfileResponse adminProfileResponse = UserProfileResponse.builder()
                .id("admin123")
                .email("admin@test.com")
                .nickname("관리자")
                .role(Role.ADMIN)
                .createdAt(testDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                .writtenReviews(new ArrayList<>())
                .likedReviews(new ArrayList<>())
                .build();

        given(userService.getComprehensiveUserProfile("admin@test.com"))
                .willReturn(adminProfileResponse);

        // when & then
        mockMvc.perform(get("/users/me")
                        .principal(adminAuth))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value("admin123"))
                .andExpect(jsonPath("$.email").value("admin@test.com"))
                .andExpect(jsonPath("$.nickname").value("관리자"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService, times(1)).getComprehensiveUserProfile("admin@test.com");
    }

    @Test
    @DisplayName("PUT /users/me - 닉네임만 수정 성공")
    void updateMyProfile_NicknameOnly() throws Exception {
        // given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setNickname("업데이트됨");
        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("test@test.com");

        User updatedUser = new User();
        updatedUser.setId("user123");
        updatedUser.setEmail("test@test.com");
        updatedUser.setNickname("업데이트됨");
        updatedUser.setPasswordHash("encodedPassword");
        updatedUser.setCreatedAt(testDateTime);
        updatedUser.setUpdatedAt(testDateTime);

        given(userService.updateUserProfile(eq("test@test.com"), any(UserUpdateRequest.class)))
                .willReturn(updatedUser);

        // when & then
        mockMvc.perform(put("/users/me")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value("user123"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("업데이트됨"))
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(userService, times(1)).updateUserProfile(eq("test@test.com"), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /users/me - 비밀번호 변경 포함 수정 성공")
    void updateMyProfile_WithPasswordChange() throws Exception {
        // given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setNickname("업데이트됨");
        updateRequest.setCurrentPassword("oldPass123");
        updateRequest.setPassword("newPass123!");

        User updatedUser = new User();
        updatedUser.setId("user123");
        updatedUser.setEmail("test@test.com");
        updatedUser.setNickname("업데이트됨");
        updatedUser.setPasswordHash("encodedPassword");
        updatedUser.setCreatedAt(testDateTime);
        updatedUser.setUpdatedAt(testDateTime);

        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("test@test.com");
        given(userService.updateUserProfile(eq("test@test.com"), any(UserUpdateRequest.class)))
                .willReturn(updatedUser);

        // when & then
        mockMvc.perform(put("/users/me")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value("user123"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("업데이트됨"))
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(userService, times(1)).updateUserProfile(eq("test@test.com"), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /users/me - 비밀번호 길이 부족으로 수정 실패")
    void updateMyProfile_ShortPassword() throws Exception {
        // given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setNickname("업데이트됨");
        updateRequest.setCurrentPassword("oldPass123");
        updateRequest.setPassword("short"); // 8자 미만
        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("test@test.com");

        // when & then
        mockMvc.perform(put("/users/me")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserProfile(anyString(), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /users/me - 잘못된 현재 비밀번호로 수정 실패")
    void updateMyProfile_InvalidCurrentPassword() throws Exception {
        // given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setNickname("업데이트됨");
        updateRequest.setCurrentPassword("wrongPassword");
        updateRequest.setPassword("newPass123!");

        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("test@test.com");
        given(userService.updateUserProfile(eq("test@test.com"), any(UserUpdateRequest.class)))
                .willThrow(new RuntimeException("현재 비밀번호가 일치하지 않습니다"));

        // when & then
        mockMvc.perform(put("/users/me")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).updateUserProfile(eq("test@test.com"), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /users/me - 비밀번호 검증 후 탈퇴 성공")
    void deleteMyAccount_Success() throws Exception {
        // given
        DeleteUserRequest deleteRequest = new DeleteUserRequest();
        deleteRequest.setPassword("password123");

        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("test@test.com");

        willDoNothing().given(userService).deleteUser(eq("test@test.com"), eq("password123"));

        // when & then
        mockMvc.perform(delete("/users/me")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(eq("test@test.com"), eq("password123"));
    }

    @Test
    @DisplayName("DELETE /users/me - 잘못된 비밀번호로 탈퇴 실패")
    void deleteMyAccount_InvalidPassword() throws Exception {
        // given
        DeleteUserRequest deleteRequest = new DeleteUserRequest();
        deleteRequest.setPassword("wrongPassword");

        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("test@test.com");

        willThrow(new RuntimeException("비밀번호가 일치하지 않습니다"))
                .given(userService).deleteUser(eq("test@test.com"), eq("wrongPassword"));

        // when & then
        mockMvc.perform(delete("/users/me")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(userService, times(1)).deleteUser(eq("test@test.com"), eq("wrongPassword"));
    }

    @Test
    @DisplayName("DELETE /users/me - 비밀번호 누락으로 탈퇴 실패")
    void deleteMyAccount_MissingPassword() throws Exception {
        // given
        DeleteUserRequest deleteRequest = new DeleteUserRequest();
        // password 누락
        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("test@test.com");

        // when & then
        mockMvc.perform(delete("/users/me")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(userService, never()).deleteUser(anyString(), anyString());
    }

    @Test
    @DisplayName("PUT /users/me - 빈 요청으로 수정 시도")
    void updateMyProfile_EmptyRequest() throws Exception {
        // given
        UserUpdateRequest emptyRequest = new UserUpdateRequest();

        User unchangedUser = new User();
        unchangedUser.setId("user123");
        unchangedUser.setEmail("test@test.com");
        unchangedUser.setNickname("테스터");
        unchangedUser.setPasswordHash("encodedPassword");
        unchangedUser.setCreatedAt(testDateTime);
        unchangedUser.setUpdatedAt(testDateTime);

        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("test@test.com");
        
        given(userService.updateUserProfile(eq("test@test.com"), any(UserUpdateRequest.class)))
                .willReturn(unchangedUser);

        // when & then
        mockMvc.perform(put("/users/me")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value("user123"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"));

        verify(userService, times(1)).updateUserProfile(eq("test@test.com"), any(UserUpdateRequest.class));
    }
}