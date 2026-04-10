package com.fundoonotes.exception;

/**
 * Exception thrown when an invalid or expired OTP is provided.
 */
public class InvalidOtpException extends RuntimeException {

    public InvalidOtpException(String message) {
        super(message);
    }
}
