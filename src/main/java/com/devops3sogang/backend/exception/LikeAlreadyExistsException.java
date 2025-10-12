package com.devops3sogang.backend.exception;

public class LikeAlreadyExistsException extends RuntimeException {
    public LikeAlreadyExistsException(String userId, String reviewId) {
        super("User " + userId + " has already liked review " + reviewId);
    }
}
