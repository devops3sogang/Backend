package com.devops3sogang.backend.exception;

public class MenuNotFoundInRestaurantException extends RuntimeException {
    public MenuNotFoundInRestaurantException(String menuId) {
        super("식당에서 해당 메뉴를 찾을 수 없습니다. 메뉴 ID: " + menuId);
    }
}