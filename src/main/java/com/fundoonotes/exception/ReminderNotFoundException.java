package com.fundoonotes.exception;

/**
 * Exception thrown when a requested reminder is not found.
 */
public class ReminderNotFoundException extends RuntimeException {

    public ReminderNotFoundException(String message) {
        super(message);
    }
}
