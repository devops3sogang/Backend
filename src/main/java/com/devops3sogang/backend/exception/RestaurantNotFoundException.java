package com.devops3sogang.backend.exception;

/**
 * 식당을 찾을 수 없을 때 발생하는 커스텀 예외
 */
public class RestaurantNotFoundException extends RuntimeException {
    public RestaurantNotFoundException(String id) {
        super("식당 정보를 찾을 수 없습니다. ID: " + id);
    }
    
    public RestaurantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}