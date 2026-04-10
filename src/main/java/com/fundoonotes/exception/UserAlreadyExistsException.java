package com.fundoonotes.exception;

/**
 * Thrown when a user attempts to register with an email that already exists.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
