package com.devops3sogang.backend.exception;

public class DuplicateRestaurantException extends RuntimeException {
    public DuplicateRestaurantException(String message) {
        super(message);
    }
}