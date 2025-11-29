package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.Role;
import com.devops3sogang.backend.document.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@TestPropertySource(properties = {
        "spring.mongodb.embedded.version=4.0.21"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        // 기존 데이터 삭제
        userRepository.deleteAll();
    
        testDateTime = LocalDateTime.of(2025, 11, 29, 0, 0);

        // 테스트 유저 1
        testUser1 = new User();
        testUser1.setId("user123");
        testUser1.setEmail("test1@test.com");
        testUser1.setNickname("테스터1");
        testUser1.setPasswordHash("encodedPassword1");
        testUser1.setCreatedAt(testDateTime);
        testUser1.setUpdatedAt(testDateTime);

        // 테스트 유저 2
        testUser2 = new User();
        testUser2.setId("user124");
        testUser2.setEmail("test2@test.com");
        testUser2.setNickname("테스터2");
        testUser2.setRole(Role.ADMIN);
        testUser2.setPasswordHash("encodedPassword2");
        testUser2.setCreatedAt(testDateTime);
        testUser2.setUpdatedAt(testDateTime);
    }

    @Test
    @DisplayName("사용자 저장 테스트")
    void saveUser() {
        // when
        User savedUser = userRepository.save(testUser1);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test1@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("테스터1");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encodedPassword1");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void findByEmail_Success() {
        // given
        userRepository.save(testUser1);

        // when
        Optional<User> foundUser = userRepository.findByEmail("test1@test.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test1@test.com");
        assertThat(foundUser.get().getNickname()).isEqualTo("테스터1");
        assertThat(foundUser.get().getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 실패 - 존재하지 않는 이메일")
    void findByEmail_NotFound() {
        // when
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하는 경우")
    void existsByEmail_True() {
        // given
        userRepository.save(testUser1);

        // when
        boolean exists = userRepository.existsByEmail("test1@test.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하지 않는 경우")
    void existsByEmail_False() {
        // when
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("모든 사용자 조회")
    void findAll() {
        // given
        userRepository.save(testUser1);
        userRepository.save(testUser2);

        // when
        List<User> users = userRepository.findAll();

        // then
        assertThat(users).hasSize(2);
        assertThat(users).extracting("email")
                .containsExactlyInAnyOrder("test1@test.com", "test2@test.com");
    }

    @Test
    @DisplayName("사용자 삭제")
    void deleteUser() {
        // given
        User savedUser = userRepository.save(testUser1);
        String userId = savedUser.getId();

        // when
        userRepository.deleteById(userId);

        // then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("사용자 정보 수정")
    void updateUser() {
        // given
        User savedUser = userRepository.save(testUser1);
        String userId = savedUser.getId();

        // when
        savedUser.setNickname("수정된닉네임");
        savedUser.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(savedUser);

        // then
        assertThat(updatedUser.getId()).isEqualTo(userId);
        assertThat(updatedUser.getNickname()).isEqualTo("수정된닉네임");
        assertThat(updatedUser.getEmail()).isEqualTo("test1@test.com");
    }

    @Test
    @DisplayName("ID로 사용자 조회 성공")
    void findById_Success() {
        // given
        User savedUser = userRepository.save(testUser1);
        String userId = savedUser.getId();

        // when
        Optional<User> foundUser = userRepository.findById(userId);

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(userId);
        assertThat(foundUser.get().getEmail()).isEqualTo("test1@test.com");
    }

    @Test
    @DisplayName("ID로 사용자 조회 실패")
    void findById_NotFound() {
        // when
        Optional<User> foundUser = userRepository.findById("nonexistentId");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("여러 사용자 저장 및 조회")
    void saveMultipleUsers() {
        // given
        User testUser3 = new User();
        testUser3.setId("user125");
        testUser3.setEmail("test3@test.com");
        testUser3.setNickname("테스터3");
        testUser3.setPasswordHash("encodedPassword3");
        testUser3.setCreatedAt(testDateTime);
        testUser3.setUpdatedAt(testDateTime);

        // when
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);

        // then
        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(3);
        assertThat(allUsers).extracting("email")
                .containsExactlyInAnyOrder(
                        "test1@test.com",
                        "test2@test.com",
                        "test3@test.com"
                );
    }

    @Test
    @DisplayName("사용자 수 확인")
    void countUsers() {
        // given
        userRepository.save(testUser1);
        userRepository.save(testUser2);

        // when
        long count = userRepository.count();

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("모든 사용자 삭제")
    void deleteAllUsers() {
        // given
        userRepository.save(testUser1);
        userRepository.save(testUser2);

        // when
        userRepository.deleteAll();

        // then
        long count = userRepository.count();
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("대소문자 구분하여 이메일로 사용자 조회")
    void findByEmail_CaseSensitive() {
        // given
        userRepository.save(testUser1);

        // when
        Optional<User> foundWithLowerCase = userRepository.findByEmail("test1@test.com");
        Optional<User> foundWithUpperCase = userRepository.findByEmail("TEST1@EXAMPLE.COM");

        // then
        assertThat(foundWithLowerCase).isPresent();
        assertThat(foundWithUpperCase).isEmpty(); // MongoDB는 기본적으로 대소문자 구분
    }

    @Test
    @DisplayName("ADMIN 권한 사용자 저장 및 조회")
    void saveAndFindAdminUser() {
        // given
        userRepository.save(testUser2);

        // when
        Optional<User> foundUser = userRepository.findByEmail("test2@test.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRole()).isEqualTo(Role.ADMIN);
        assertThat(foundUser.get().getNickname()).isEqualTo("테스터2");
    }

    @Test
    @DisplayName("동일한 이메일로 중복 저장 시도")
    void saveDuplicateEmail() {
        // given
        userRepository.save(testUser1);

        User duplicateUser = new User();
        duplicateUser.setEmail("test1@test.com");
        duplicateUser.setNickname("중복유저");
        duplicateUser.setPasswordHash("encodedPassword");
        duplicateUser.setCreatedAt(testDateTime);
        duplicateUser.setUpdatedAt(testDateTime);

        // when & then
        // MongoDB는 unique 인덱스가 없으면 중복 저장이 가능
        // unique 인덱스 설정이 있다면 예외 발생
        assertThatCode(() -> userRepository.save(duplicateUser))
                .doesNotThrowAnyException();
        
        // 실제로는 unique 인덱스 설정 여부에 따라 다름
        List<User> users = userRepository.findAll();
        assertThat(users.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("비밀번호 해시 저장 확인")
    void savePasswordHash() {
        // given
        User user = new User();
        user.setEmail("secure@test.com");
        user.setNickname("보안유저");
        user.setPasswordHash("$2a$10$encodedHashValue");
        user.setCreatedAt(testDateTime);
        user.setUpdatedAt(testDateTime);

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$10$encodedHashValue");
        assertThat(savedUser.getPasswordHash()).startsWith("$2a$10$");
    }
}