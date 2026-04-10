package com.fundoonotes.exception;

/**
 * Thrown when login credentials (email or password) are incorrect.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
