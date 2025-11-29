package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.config.jwt.JwtUtil;
import com.devops3sogang.backend.document.RefreshToken;
import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.LoginRequest;
import com.devops3sogang.backend.dto.LoginResponse;
import com.devops3sogang.backend.dto.RegisterRequest;
import com.devops3sogang.backend.dto.RegisterResponse;
import com.devops3sogang.backend.service.AuthService;
import com.devops3sogang.backend.service.RefreshTokenService;
import org.springframework.security.core.Authentication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser; 
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import java.time.LocalDateTime;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import java.util.List;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean(name = "mongoAuditingHandler")
    private Object mongoAuditingHandler;

    private RegisterRequest registerRequest;
    private RegisterResponse registerResponse;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 회원가입 요청 객체
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123!");
        registerRequest.setNickname("테스트유저");

        // 회원가입 응답 객체
        registerResponse = new RegisterResponse();
        registerResponse.set_id("user123");
        registerResponse.setEmail("test@example.com");
        registerResponse.setNickname("테스트유저");
        registerResponse.setRole(Role.USER);
        registerResponse.setCreatedAt(LocalDateTime.now());
        registerResponse.setUpdatedAt(LocalDateTime.now());

        // 로그인 요청 객체
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123!");

        // 로그인 응답 객체
        loginResponse = new LoginResponse();
        loginResponse.setAccessToken("access-token-123");
        loginResponse.setRefreshToken("refresh-token-456");
        loginResponse.setTokenType("Bearer");
        loginResponse.setExpiresAt(System.currentTimeMillis() + 3600000);
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.set_id("user123");
        userInfo.setEmail("test@example.com");
        userInfo.setNickname("테스트유저");
        userInfo.setRole(Role.USER);
        loginResponse.setUser(userInfo);

        // 테스트 유저 (UserDetails 구현체)
        testUser = new User();
        testUser.setId("user123");
        testUser.setEmail("test@example.com");
        testUser.setNickname("테스트유저");
        testUser.setPasswordHash("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void registerSuccess() throws Exception {
        // given
        given(authService.register(any(RegisterRequest.class))).willReturn(registerResponse);

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._id").value("user123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 누락")
    void registerFail_MissingEmail() throws Exception {
        // given
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setPassword("password123!");
        invalidRequest.setNickname("테스트유저");
        // email 누락

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 잘못된 이메일 형식")
    void registerFail_InvalidEmailFormat() throws Exception {
        // given
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123!");
        invalidRequest.setNickname("테스트유저");

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 길이 부족")
    void registerFail_ShortPassword() throws Exception {
        // given
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("short");
        invalidRequest.setNickname("테스트유저");

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 누락")
    void registerFail_MissingNickname() throws Exception {
        // given
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123!");
        // nickname 누락

        // when & then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() throws Exception {
        // given
        given(authService.login(any(LoginRequest.class))).willReturn(loginResponse);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.user._id").value("user123"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.user.role").value("USER"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 누락")
    void loginFail_MissingEmail() throws Exception {
        // given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setPassword("password123!");
        // email 누락

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 누락")
    void loginFail_MissingPassword() throws Exception {
        // given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("test@example.com");
        // password 누락

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 자격증명")
    void loginFail_InvalidCredentials() throws Exception {
        // given
        given(authService.login(any(LoginRequest.class)))
                .willThrow(new RuntimeException("Invalid credentials"));

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("토큰 갱신 성공 테스트")
    void refreshTokenSuccess() throws Exception {
        // given
        String oldRefreshToken = "old-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        RefreshToken tokenRecord = new RefreshToken();
        tokenRecord.setEmail("test@example.com");
        tokenRecord.setTokenHash(oldRefreshToken);
        tokenRecord.setExpiresAt(LocalDateTime.now().plusDays(7));

        given(refreshTokenService.validateRefreshToken(oldRefreshToken)).willReturn(tokenRecord);
        willDoNothing().given(refreshTokenService).revoke(tokenRecord);
        given(jwtUtil.createToken("test@example.com")).willReturn(newAccessToken);
        given(refreshTokenService.generateRefreshToken("test@example.com")).willReturn(newRefreshToken);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + oldRefreshToken + "\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                .andExpect(jsonPath("$.refreshToken").value(newRefreshToken));

        verify(refreshTokenService, times(1)).validateRefreshToken(oldRefreshToken);
        verify(refreshTokenService, times(1)).revoke(tokenRecord);
        verify(jwtUtil, times(1)).createToken("test@example.com");
        verify(refreshTokenService, times(1)).generateRefreshToken("test@example.com");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 리프레시 토큰")
    void refreshTokenFail_InvalidToken() throws Exception {
        // given
        String invalidRefreshToken = "invalid-refresh-token";
        given(refreshTokenService.validateRefreshToken(invalidRefreshToken)).willReturn(null);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + invalidRefreshToken + "\"}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid refresh token"));

        verify(refreshTokenService, times(1)).validateRefreshToken(invalidRefreshToken);
        verify(refreshTokenService, never()).revoke(any());
        verify(jwtUtil, never()).createToken(anyString());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 만료된 리프레시 토큰")
    void refreshTokenFail_ExpiredToken() throws Exception {
        // given
        String expiredRefreshToken = "expired-refresh-token";
        
        RefreshToken expiredTokenRecord = new RefreshToken();
        expiredTokenRecord.setEmail("test@example.com");
        expiredTokenRecord.setTokenHash(expiredRefreshToken);
        expiredTokenRecord.setExpiresAt(LocalDateTime.now().minusDays(1));

        given(refreshTokenService.validateRefreshToken(expiredRefreshToken)).willReturn(expiredTokenRecord);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + expiredRefreshToken + "\"}"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid refresh token"));

        verify(refreshTokenService, times(1)).validateRefreshToken(expiredRefreshToken);
        verify(refreshTokenService, never()).revoke(any());
        verify(jwtUtil, never()).createToken(anyString());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - refreshToken 파라미터 누락")
    void refreshTokenFail_MissingRefreshToken() throws Exception {
        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        verify(refreshTokenService, never()).validateRefreshToken(anyString());
    }

    // Note: logout test skipped - requires Spring Security authentication context
}