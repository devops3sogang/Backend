package com.devops3sogang.backend.service;

import com.devops3sogang.backend.document.OnCampusMenu;

public interface OnCampusMenuService {
    OnCampusMenu findMenuByWeekStartDate(String weekStartDate);
}