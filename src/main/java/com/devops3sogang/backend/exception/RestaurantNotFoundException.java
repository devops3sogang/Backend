package com.devops3sogang.backend.exception;

public class RestaurantNotFoundException extends RuntimeException {
    public RestaurantNotFoundException(String id) {
        super("식당 정보를 찾을 수 없습니다. ID: " + id);
    }
    
    public RestaurantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}