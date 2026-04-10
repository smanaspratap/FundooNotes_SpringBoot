package com.fundoonotes.exception;

/**
 * Thrown when a user attempts to access a resource they do not own,
 * or when an authorization token is missing/invalid.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
