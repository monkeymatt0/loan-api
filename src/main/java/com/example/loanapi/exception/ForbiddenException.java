package com.example.loanapi.exception;

/**
 * Exception thrown when user is forbidden from accessing a resource
 * Used when user tries to access resources owned by another user
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
