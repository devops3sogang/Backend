package com.devops3sogang.backend.exception;

public class MenuNotFoundException extends RuntimeException {
    public MenuNotFoundException(String weekStartDate) {
        super("해당 주의 메뉴를 찾을 수 없습니다. Week: " + weekStartDate);
    }
}