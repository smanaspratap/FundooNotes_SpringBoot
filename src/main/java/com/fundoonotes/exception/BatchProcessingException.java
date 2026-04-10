package com.fundoonotes.exception;

/**
 * Exception thrown when a batch processing operation fails.
 */
public class BatchProcessingException extends RuntimeException {

    public BatchProcessingException(String message) {
        super(message);
    }
}
