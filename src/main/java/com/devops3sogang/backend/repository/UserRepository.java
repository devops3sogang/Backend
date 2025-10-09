package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    // 이메일로 사용자를 찾는 기능 (올바른 사용 예)
    Optional<User> findByEmail(String email);

    // 이메일 존재 여부를 확인하는 기능 (성능상 이점)
    boolean existsByEmail(String email);

    // findById, deleteById, save, findAll 등은 MongoRepository가 이미 가지고 있으므로
    // 별도로 작성할 필요가 없습니다.
}