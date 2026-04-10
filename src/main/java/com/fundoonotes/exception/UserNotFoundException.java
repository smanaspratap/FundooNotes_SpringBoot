package com.fundoonotes.exception;

/**
 * Thrown when a user cannot be found by email or ID.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
