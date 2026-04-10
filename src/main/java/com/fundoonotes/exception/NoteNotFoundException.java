package com.fundoonotes.exception;

/**
 * Thrown when a note cannot be found by its ID.
 */
public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(String message) {
        super(message);
    }
}
