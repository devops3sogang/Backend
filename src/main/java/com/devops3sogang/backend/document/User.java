package com.devops3sogang.backend.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
@Document(collection = "users")
public class User implements UserDetails { // UserDetails 구현
    @Id
    private String id;
    private String email;
    private String passwordHash;
    private String nickname;
    private String role; // 예: "USER", "ADMIN"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- UserDetails 인터페이스의 메서드 구현 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 role을 Spring Security가 이해하는 권한으로 변환
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        // 우리 시스템에서는 email을 username처럼 사용
        return this.email;
    }

    // 계정 만료 여부 (여기서는 항상 사용 가능하도록 true 반환)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 자격 증명(비밀번호) 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부
    @Override
    public boolean isEnabled() {
        return true;
    }
}