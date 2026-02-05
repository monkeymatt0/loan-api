package com.example.loanapi.exception;

/**
 * Exception thrown when user is not authorized to access a resource
 * Used for missing/invalid tokens or insufficient role permissions
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
