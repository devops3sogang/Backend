package com.devops3sogang.backend.service;

import com.devops3sogang.backend.config.jwt.JwtUtil;
import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.LoginRequest;
import com.devops3sogang.backend.dto.LoginResponse;
import com.devops3sogang.backend.dto.RegisterRequest;
import com.devops3sogang.backend.dto.RegisterResponse;
import com.devops3sogang.backend.exception.DuplicateEmailException;
import com.devops3sogang.backend.exception.InvalidCredentialsException;
import com.devops3sogang.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user1@sogang.ac.kr");
        request.setNickname("김철수");
        request.setPassword("password");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_password");

        User savedUser = new User();
        savedUser.setId("123");
        savedUser.setEmail(request.getEmail());
        savedUser.setNickname(request.getNickname());
        savedUser.setPasswordHash("hashed_password");
        savedUser.setRole(Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.get_id()).isEqualTo("123");
        assertThat(response.getEmail()).isEqualTo("user1@sogang.ac.kr");
        assertThat(response.getNickname()).isEqualTo("김철수");
        assertThat(response.getRole()).isEqualTo(Role.USER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("user1@sogang.ac.kr");
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void register_duplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user1@sogang.ac.kr");
        request.setNickname("김철수");
        request.setPassword("password");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user1@sogang.ac.kr");
        request.setPassword("password");

        User user = new User();
        user.setId("123");
        user.setEmail(request.getEmail());
        user.setNickname("김철수");
        user.setPasswordHash("hashed_password");
        user.setRole(Role.USER);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), "hashed_password")).thenReturn(true);
        when(jwtUtil.createToken(user.getEmail())).thenReturn("jwt_token");
        when(jwtUtil.getExpirationTimeMillis("jwt_token")).thenReturn(3600000L);

        LoginResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getExpiresAt()).isEqualTo(3600000L);
        assertThat(response.getUser().get_id()).isEqualTo("123");
        assertThat(response.getUser().getEmail()).isEqualTo("user1@sogang.ac.kr");
        assertThat(response.getUser().getNickname()).isEqualTo("김철수");
        assertThat(response.getUser().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_userNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user1@sogang.ac.kr");
        request.setPassword("password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user1@sogang.ac.kr");
        request.setPassword("wrong_password");

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash("hashed_password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), "hashed_password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}