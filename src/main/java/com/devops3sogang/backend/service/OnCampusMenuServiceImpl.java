package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.OnCampusMenu;
import com.devops3sogang.backend.exception.MenuNotFoundException;
import com.devops3sogang.backend.repository.OnCampusMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnCampusMenuServiceImpl implements OnCampusMenuService {
    private final OnCampusMenuRepository onCampusMenuRepository;

    @Override
    public OnCampusMenu findMenuByWeekStartDate(String weekStartDate) {
        log.info("주간 메뉴 조회 시작 - weekStartDate: {}", weekStartDate);
        
        OnCampusMenu menu = onCampusMenuRepository.findByWeekStartDate(weekStartDate)
                .orElseThrow(() -> {
                    log.warn("주간 메뉴를 찾을 수 없음 - weekStartDate: {}", weekStartDate);
                    return new MenuNotFoundException(weekStartDate);
                });
        
        log.info("주간 메뉴 조회 완료 - 식당: {}", menu.getRestaurantName());
        return menu;
    }
}