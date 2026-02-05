package com.example.loanapi.exception;

/**
 * Custom exception for when a loan request is not found
 */
public class LoanRequestNotFoundException extends RuntimeException {

    public LoanRequestNotFoundException(String message) {
        super(message);
    }

    public LoanRequestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoanRequestNotFoundException(Long id) {
        super("Loan request with ID " + id + " not found");
    }
}
