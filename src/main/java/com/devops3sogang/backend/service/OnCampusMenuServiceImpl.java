package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.OnCampusMenu;
import com.devops3sogang.backend.repository.OnCampusMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OnCampusMenuServiceImpl implements OnCampusMenuService {

    private final OnCampusMenuRepository onCampusMenuRepository;

    @Override
    public OnCampusMenu findMenuByWeekStartDate(String weekStartDate) {
        return onCampusMenuRepository.findByWeekStartDate(weekStartDate)
                .orElseThrow(() -> new RuntimeException("해당 주의 메뉴를 찾을 수 없습니다."));
    }
}