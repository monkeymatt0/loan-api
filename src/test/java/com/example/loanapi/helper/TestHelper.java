package com.example.loanapi.helper;

import com.example.loanapi.dto.CreateLoanRequestDTO;
import com.example.loanapi.dto.UpdateLoanRequestDTO;
import com.example.loanapi.dto.UpdateLoanRequestStatusDTO;
import com.example.loanapi.model.LoanRequest;
import com.example.loanapi.model.User;
import com.example.loanapi.model.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Utility class for test data creation and helper methods
 */
public class TestHelper {

    // Predefined test users
    public static final Long CLIENTE_ID = 1L;
    public static final Long GESTORE_ID = 2L;
    public static final String CLIENTE_TOKEN = "test-cliente-token";
    public static final String GESTORE_TOKEN = "test-gestore-token";

    /**
     * Create a test CLIENTE user
     */
    public static User createClienteUser() {
        return new User(CLIENTE_ID, "Test Cliente", "cliente@test.com", UserRole.CLIENTE, CLIENTE_TOKEN);
    }

    /**
     * Create a test GESTORE user
     */
    public static User createGestoreUser() {
        return new User(GESTORE_ID, "Test Gestore", "gestore@test.com", UserRole.GESTORE, GESTORE_TOKEN);
    }

    /**
     * Create a valid CreateLoanRequestDTO for testing
     */
    public static CreateLoanRequestDTO createValidCreateDTO() {
        CreateLoanRequestDTO dto = new CreateLoanRequestDTO();
        dto.setApplicantName("John Doe");
        dto.setAmount(new BigDecimal("1000.00"));
        dto.setCurrency("EUR");
        dto.setIdentityDocument("ABC12345");
        return dto;
    }

    /**
     * Create a CreateLoanRequestDTO with custom values
     */
    public static CreateLoanRequestDTO createCreateDTO(String name, BigDecimal amount, String currency, String document) {
        CreateLoanRequestDTO dto = new CreateLoanRequestDTO();
        dto.setApplicantName(name);
        dto.setAmount(amount);
        dto.setCurrency(currency);
        dto.setIdentityDocument(document);
        return dto;
    }

    /**
     * Create an invalid CreateLoanRequestDTO (missing required fields)
     */
    public static CreateLoanRequestDTO createInvalidCreateDTO() {
        CreateLoanRequestDTO dto = new CreateLoanRequestDTO();
        // All fields are null/invalid
        return dto;
    }

    /**
     * Create a valid UpdateLoanRequestDTO for testing
     */
    public static UpdateLoanRequestDTO createValidUpdateDTO() {
        UpdateLoanRequestDTO dto = new UpdateLoanRequestDTO();
        dto.setApplicantName("Jane Doe");
        dto.setAmount(new BigDecimal("2000.00"));
        dto.setCurrency("USD");
        dto.setIdentityDocument("CDE78901");
        return dto;
    }

    /**
     * Create a valid UpdateLoanRequestStatusDTO for testing
     */
    public static UpdateLoanRequestStatusDTO createStatusUpdateDTO(String status) {
        UpdateLoanRequestStatusDTO dto = new UpdateLoanRequestStatusDTO();
        dto.setStatus(status);
        return dto;
    }

    /**
     * Create a LoanRequest model for testing
     */
    public static LoanRequest createLoanRequest(Long id, Long userId, String status) {
        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setId(id);
        loanRequest.setUserId(userId);
        loanRequest.setApplicantName("Test Applicant");
        loanRequest.setAmount(new BigDecimal("1000.00"));
        loanRequest.setCurrency("EUR");
        loanRequest.setIdentityDocument("TEST123");
        loanRequest.setStatus(status);
        loanRequest.setCreatedAt(LocalDateTime.now());
        return loanRequest;
    }

    /**
     * Create a LoanRequest model with custom values
     */
    public static LoanRequest createLoanRequest(Long id, Long userId, String applicantName, 
                                                 BigDecimal amount, String currency, String document, 
                                                 String status, LocalDateTime createdAt) {
        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setId(id);
        loanRequest.setUserId(userId);
        loanRequest.setApplicantName(applicantName);
        loanRequest.setAmount(amount);
        loanRequest.setCurrency(currency);
        loanRequest.setIdentityDocument(document);
        loanRequest.setStatus(status);
        loanRequest.setCreatedAt(createdAt);
        return loanRequest;
    }
}
