package com.devops3sogang.backend.exception;

public class RestaurantAlreadyExistsException extends RuntimeException {
    public RestaurantAlreadyExistsException(String name, String address) {
        super("Restaurant with name '" + name + "' and address '" + address + "' already exists.");
    }
}
