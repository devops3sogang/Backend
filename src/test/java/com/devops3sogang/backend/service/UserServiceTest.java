package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.Like;
import com.devops3sogang.backend.document.Review;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.UserProfileResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;
import com.devops3sogang.backend.exception.UserNotFoundException;
import com.devops3sogang.backend.repository.LikeRepository;
import com.devops3sogang.backend.repository.ReviewRepository;
import com.devops3sogang.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId("user123");
        mockUser.setEmail("test@sogang.ac.kr");
        mockUser.setNickname("테스트유저");
        mockUser.setPasswordHash("hashedpass");
    }

    @Test
    void testGetComprehensiveUserProfile_Success() {
        when(userRepository.findByEmail("test@sogang.ac.kr")).thenReturn(Optional.of(mockUser));
        when(reviewRepository.findByUserId("user123")).thenReturn(Collections.emptyList());
        when(likeRepository.findByUserId("user123")).thenReturn(Collections.emptyList());

        UserProfileResponse response = userService.getComprehensiveUserProfile("test@sogang.ac.kr");

        assertEquals(mockUser.getId(), response.getId());
        assertEquals(mockUser.getEmail(), response.getEmail());
        assertEquals(mockUser.getNickname(), response.getNickname());
        assertTrue(response.getWrittenReviews().isEmpty());
        assertTrue(response.getLikedReviews().isEmpty());

        verify(userRepository, times(1)).findByEmail("test@sogang.ac.kr");
    }

    @Test
    void testUpdateUserProfile_ChangeNicknameAndPassword() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("새닉네임");
        request.setPassword("newpassword");

        when(userRepository.findByEmail("test@sogang.ac.kr")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedPass");
        when(reviewRepository.findByUserId("user123")).thenReturn(Collections.emptyList());

        User updatedUser = userService.updateUserProfile("test@sogang.ac.kr", request);

        assertEquals("새닉네임", updatedUser.getNickname());
        assertEquals("encodedPass", updatedUser.getPasswordHash());
        assertNotNull(updatedUser.getUpdatedAt());

        verify(userRepository, times(1)).save(updatedUser);
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.findByEmail("test@sogang.ac.kr")).thenReturn(Optional.of(mockUser));
        when(reviewRepository.findByUserId("user123")).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> userService.deleteUser("test@sogang.ac.kr"));

        verify(userRepository, times(1)).delete(mockUser);
        verify(likeRepository, times(1)).deleteByUserId("user123");
    }

    @Test
    void testGetComprehensiveUserProfile_UserNotFound() {
        when(userRepository.findByEmail("unknown@sogang.ac.kr")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getComprehensiveUserProfile("unknown@sogang.ac.kr"));
    }
}