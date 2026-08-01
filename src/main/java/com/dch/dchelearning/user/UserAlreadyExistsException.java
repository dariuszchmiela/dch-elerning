package com.dch.dchelearning.user;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super("User with this email already exists: " + email);
    }
}
