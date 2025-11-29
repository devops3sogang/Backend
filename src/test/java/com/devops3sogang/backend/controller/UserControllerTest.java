package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.DeleteUserRequest;
import com.devops3sogang.backend.dto.UserProfileResponse;
import com.devops3sogang.backend.dto.UserUpdateRequest;
import com.devops3sogang.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetMyProfile() throws Exception {
        when(authentication.getName()).thenReturn("user1@sogang.ac.kr");

        UserProfileResponse mockProfile = UserProfileResponse.builder()
                .id("507f191e810c19729de860e1")
                .email("user1@sogang.ac.kr")
                .nickname("김철수")
                .role(Role.USER)
                .createdAt("2025-01-01T00:00:00")
                .writtenReviews(Collections.emptyList())
                .likedReviews(Collections.emptyList())
                .build();

        when(userService.getComprehensiveUserProfile("user1@sogang.ac.kr"))
                .thenReturn(mockProfile);

        mockMvc.perform(get("/users/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user1@sogang.ac.kr"))
                .andExpect(jsonPath("$.nickname").value("김철수"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService, times(1))
                .getComprehensiveUserProfile("user1@sogang.ac.kr");
    }

    @Test
    void testUpdateMyProfile() throws Exception {
        when(authentication.getName()).thenReturn("user1@sogang.ac.kr");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNickname("새닉네임");
        request.setPassword("newpassword123");

        User updatedUser = new User();
        updatedUser.setId("507f191e810c19729de860e1");
        updatedUser.setEmail("user1@sogang.ac.kr");
        updatedUser.setNickname("새닉네임");
        updatedUser.setUpdatedAt(LocalDateTime.now());

        when(userService.updateUserProfile(eq("user1@sogang.ac.kr"), any(UserUpdateRequest.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/users/me")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("새닉네임"))
                .andExpect(jsonPath("$._id").value("507f191e810c19729de860e1"));

        verify(userService, times(1))
                .updateUserProfile(eq("user1@sogang.ac.kr"), any(UserUpdateRequest.class));
    }

    @Test
    void testDeleteMyAccount() throws Exception {
        when(authentication.getName()).thenReturn("user1@sogang.ac.kr");

        DeleteUserRequest request = new DeleteUserRequest();
        request.setPassword("password123");

        doNothing().when(userService).deleteUser(eq("user1@sogang.ac.kr"), anyString());

        mockMvc.perform(delete("/users/me")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(eq("user1@sogang.ac.kr"), eq("password123"));
    }
}