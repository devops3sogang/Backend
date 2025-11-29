package com.devops3sogang.backend.repository;

import com.devops3sogang.backend.document.OnCampusMenu;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OnCampusMenuRepository extends MongoRepository<OnCampusMenu, String> {
    @Query("{'weekStartDate': ?0}")
    List<OnCampusMenu> findByWeekStartDate(String weekStartDate);

    @Query("{'dailyMenus.date': ?0}")
    List<OnCampusMenu> findByDailyMenusDate(String date);
}