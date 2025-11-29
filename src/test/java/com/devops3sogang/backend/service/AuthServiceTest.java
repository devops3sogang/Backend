package com.devops3sogang.backend.service;

import com.devops3sogang.backend.config.jwt.JwtUtil;
import com.devops3sogang.backend.document.RefreshToken;
import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.document.TokenBlacklist;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.LoginRequest;
import com.devops3sogang.backend.dto.LoginResponse;
import com.devops3sogang.backend.dto.RegisterRequest;
import com.devops3sogang.backend.dto.RegisterResponse;
import com.devops3sogang.backend.exception.DuplicateEmailException;
import com.devops3sogang.backend.exception.InvalidCredentialsException;
import com.devops3sogang.backend.repository.RefreshTokenRepository;
import com.devops3sogang.backend.repository.TokenBlacklistRepository;
import com.devops3sogang.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 회원가입 요청 객체
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123!");
        registerRequest.setNickname("테스트유저");

        // 로그인 요청 객체
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123!");

        // 테스트 유저
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
    void registerSuccess() {
        // given
        given(userRepository.existsByEmail(registerRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(registerRequest.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        RegisterResponse response = authService.register(registerRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.get_id()).isEqualTo(testUser.getId());
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getNickname()).isEqualTo(testUser.getNickname());
        assertThat(response.getRole()).isEqualTo(testUser.getRole());

        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));

        // User 객체가 올바르게 생성되었는지 검증
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(savedUser.getNickname()).isEqualTo(registerRequest.getNickname());
        assertThat(savedUser.getPasswordHash()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void registerFail_DuplicateEmail() {
        // given
        given(userRepository.existsByEmail(registerRequest.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        // given
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-456";
        Long expiresAt = System.currentTimeMillis() + 3600000;

        given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).willReturn(true);
        given(jwtUtil.createToken(testUser.getEmail())).willReturn(accessToken);
        given(refreshTokenService.generateRefreshToken(testUser.getEmail())).willReturn(refreshToken);
        given(jwtUtil.getExpirationTimeMillis(accessToken)).willReturn(expiresAt);

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(response.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().get_id()).isEqualTo(testUser.getId());
        assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getUser().getNickname()).isEqualTo(testUser.getNickname());
        assertThat(response.getUser().getRole()).isEqualTo(testUser.getRole());

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtUtil, times(1)).createToken(testUser.getEmail());
        verify(refreshTokenService, times(1)).generateRefreshToken(testUser.getEmail());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void loginFail_UserNotFound() {
        // given
        given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).createToken(anyString());
        verify(refreshTokenService, never()).generateRefreshToken(anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void loginFail_InvalidPassword() {
        // given
        given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPasswordHash());
        verify(jwtUtil, never()).createToken(anyString());
        verify(refreshTokenService, never()).generateRefreshToken(anyString());
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logoutSuccess() {
        // given
        String userEmail = "test@example.com";
        String accessToken = "access-token-123";
        Long expirationMillis = System.currentTimeMillis() + 3600000;

        given(jwtUtil.getExpirationTimeMillis(accessToken)).willReturn(expirationMillis);
        willDoNothing().given(refreshTokenRepository).deleteAllByEmail(userEmail);
        given(tokenBlacklistRepository.save(any(TokenBlacklist.class))).willReturn(new TokenBlacklist());

        // when
        authService.logout(userEmail, accessToken);

        // then
        verify(refreshTokenRepository, times(1)).deleteAllByEmail(userEmail);
        verify(jwtUtil, times(1)).getExpirationTimeMillis(accessToken);
        verify(tokenBlacklistRepository, times(1)).save(any(TokenBlacklist.class));

        // TokenBlacklist 객체가 올바르게 생성되었는지 검증
        ArgumentCaptor<TokenBlacklist> blacklistCaptor = ArgumentCaptor.forClass(TokenBlacklist.class);
        verify(tokenBlacklistRepository).save(blacklistCaptor.capture());
        TokenBlacklist savedBlacklist = blacklistCaptor.getValue();
        assertThat(savedBlacklist.getToken()).isEqualTo(accessToken);
        assertThat(savedBlacklist.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("로그아웃 - Refresh Token 삭제 확인")
    void logout_DeletesRefreshTokens() {
        // given
        String userEmail = "test@example.com";
        String accessToken = "access-token-123";
        Long expirationMillis = System.currentTimeMillis() + 3600000;

        given(jwtUtil.getExpirationTimeMillis(accessToken)).willReturn(expirationMillis);
        willDoNothing().given(refreshTokenRepository).deleteAllByEmail(userEmail);
        given(tokenBlacklistRepository.save(any(TokenBlacklist.class))).willReturn(new TokenBlacklist());

        // when
        authService.logout(userEmail, accessToken);

        // then
        verify(refreshTokenRepository, times(1)).deleteAllByEmail(userEmail);
    }

    @Test
    @DisplayName("로그아웃 - Access Token 블랙리스트 추가 확인")
    void logout_AddsTokenToBlacklist() {
        // given
        String userEmail = "test@example.com";
        String accessToken = "access-token-123";
        Long expirationMillis = System.currentTimeMillis() + 3600000;

        given(jwtUtil.getExpirationTimeMillis(accessToken)).willReturn(expirationMillis);
        willDoNothing().given(refreshTokenRepository).deleteAllByEmail(userEmail);
        given(tokenBlacklistRepository.save(any(TokenBlacklist.class))).willReturn(new TokenBlacklist());

        // when
        authService.logout(userEmail, accessToken);

        // then
        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);
        verify(tokenBlacklistRepository, times(1)).save(captor.capture());
        
        TokenBlacklist blacklistedToken = captor.getValue();
        assertThat(blacklistedToken.getToken()).isEqualTo(accessToken);
        assertThat(blacklistedToken.getExpiresAt()).isNotNull();
        assertThat(blacklistedToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("회원가입 시 비밀번호 암호화 확인")
    void register_EncryptsPassword() {
        // given
        String rawPassword = "password123!";
        String encodedPassword = "encodedPassword123";
        
        registerRequest.setPassword(rawPassword);
        
        given(userRepository.existsByEmail(registerRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        authService.register(registerRequest);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        assertThat(savedUser.getPasswordHash()).isEqualTo(encodedPassword);
        assertThat(savedUser.getPasswordHash()).isNotEqualTo(rawPassword);
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("로그인 시 토큰 생성 순서 확인")
    void login_GeneratesTokensInCorrectOrder() {
        // given
        given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtUtil.createToken(testUser.getEmail())).willReturn("access-token");
        given(refreshTokenService.generateRefreshToken(testUser.getEmail())).willReturn("refresh-token");
        given(jwtUtil.getExpirationTimeMillis(anyString())).willReturn(System.currentTimeMillis());

        // when
        authService.login(loginRequest);

        // then
        var inOrder = inOrder(jwtUtil, refreshTokenService);
        inOrder.verify(jwtUtil).createToken(testUser.getEmail());
        inOrder.verify(refreshTokenService).generateRefreshToken(testUser.getEmail());
        inOrder.verify(jwtUtil).getExpirationTimeMillis(anyString());
    }
}