package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.document.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 저장 및 조회 테스트")
    void testSaveAndFindByEmail() {
        User user = new User();
        user.setEmail("user1@sogang.ac.kr");
        user.setNickname("김철수");
        user.setPasswordHash("hashed_password");
        user.setRole(Role.USER);

        // 저장
        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();

        // 이메일로 조회
        Optional<User> found = userRepository.findByEmail("user1@sogang.ac.kr");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user1@sogang.ac.kr");
        assertThat(found.get().getNickname()).isEqualTo("김철수");
        assertThat(found.get().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 테스트")
    void testExistsByEmail() {
        User user = new User();
        user.setEmail("user2@sogang.ac.kr");
        user.setNickname("홍길동");
        user.setPasswordHash("hashed_password");
        user.setRole(Role.USER);

        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("user2@sogang.ac.kr");
        assertThat(exists).isTrue();

        boolean notExists = userRepository.existsByEmail("nonexistent@sogang.ac.kr");
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("사용자 삭제 테스트")
    void testDeleteById() {
        User user = new User();
        user.setEmail("delete@sogang.ac.kr");
        user.setNickname("삭제테스트");
        user.setPasswordHash("hashed_password");
        user.setRole(Role.USER);

        User saved = userRepository.save(user);
        String id = saved.getId();

        userRepository.deleteById(id);

        Optional<User> deleted = userRepository.findById(id);
        assertThat(deleted).isEmpty();
    }
}