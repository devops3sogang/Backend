package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.dto.LoginRequest;
import com.devops3sogang.backend.dto.LoginResponse;
import com.devops3sogang.backend.dto.LoginResponse.UserInfo;
import com.devops3sogang.backend.dto.RegisterRequest;
import com.devops3sogang.backend.dto.RegisterResponse;
import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user1@sogang.ac.kr");
        request.setNickname("김철수");
        request.setPassword("password123");

        RegisterResponse mockResponse = new RegisterResponse();
        mockResponse.set_id("507f191e810c19729de860e1");
        mockResponse.setEmail("user1@sogang.ac.kr");
        mockResponse.setNickname("김철수");
        mockResponse.setRole(Role.USER);

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._id").value("507f191e810c19729de860e1"))
                .andExpect(jsonPath("$.email").value("user1@sogang.ac.kr"))
                .andExpect(jsonPath("$.nickname").value("김철수"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user1@sogang.ac.kr");
        request.setPassword("password123");

        LoginResponse.UserInfo userInfo = new UserInfo();
        userInfo.set_id("507f191e810c19729de860e1");
        userInfo.setEmail("user1@sogang.ac.kr");
        userInfo.setNickname("김철수");
        userInfo.setRole(Role.USER);

        LoginResponse mockResponse = new LoginResponse();
        mockResponse.setToken("token_here");
        mockResponse.setExpiresAt(System.currentTimeMillis() + 3600000L);
        mockResponse.setUser(userInfo);

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token_here"))
                .andExpect(jsonPath("$.user._id").value("507f191e810c19729de860e1"))
                .andExpect(jsonPath("$.user.role").value("USER"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogout() throws Exception {
      mockMvc.perform(post("/auth/logout"))
           .andDo(print())
           .andExpect(status().isOk());
    }
}