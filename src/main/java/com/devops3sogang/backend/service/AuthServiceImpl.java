package com.devops3sogang.backend.service;

import com.devops3sogang.backend.config.jwt.JwtUtil;
import com.devops3sogang.backend.document.User;
import com.devops3sogang.backend.dto.LoginRequest;
import com.devops3sogang.backend.dto.LoginResponse;
import com.devops3sogang.backend.dto.RegisterRequest;
import com.devops3sogang.backend.dto.RegisterResponse;
import com.devops3sogang.backend.exception.DuplicateEmailException;
import com.devops3sogang.backend.exception.InvalidCredentialsException;
import com.devops3sogang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        log.info("회원가입 시작 - email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("이미 가입된 이메일 - email: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        
        User saved = userRepository.save(user);
        
        log.info("회원가입 완료 - userId: {}, email: {}", saved.getId(), saved.getEmail());
        
        RegisterResponse response = new RegisterResponse();
        response.setId(saved.getId());
        response.setEmail(saved.getEmail());
        response.setNickname(saved.getNickname());
        response.setCreatedAt(saved.getCreatedAt());
        response.setMessage("회원가입이 완료되었습니다.");

        return response;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도 - email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - email: {}", request.getEmail());
                    return new InvalidCredentialsException();
                });
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("비밀번호 불일치 - email: {}", request.getEmail());
            throw new InvalidCredentialsException();
        }
        
        String token = jwtUtil.createToken(user.getEmail());
        Long expiresAt = jwtUtil.getExpirationTimeMillis(token);

        log.info("로그인 성공 - email: {}", request.getEmail());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpiresAt(expiresAt);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.set_id(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setNickname(user.getNickname());
        userInfo.setRole(user.getRole());

        response.setUser(userInfo);
        
        return response;
    }
}