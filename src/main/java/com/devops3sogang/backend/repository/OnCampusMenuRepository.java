package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.OnCampusMenu;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface OnCampusMenuRepository extends MongoRepository<OnCampusMenu, String> {
    // 날짜로 메뉴를 찾기 위한 쿼리 메서드
    Optional<OnCampusMenu> findByWeekStartDate(String weekStartDate);
}