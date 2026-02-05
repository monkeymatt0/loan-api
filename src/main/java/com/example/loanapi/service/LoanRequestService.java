package com.example.loanapi.service;

import com.example.loanapi.dto.CreateLoanRequestDTO;
import com.example.loanapi.dto.LoanRequestFilter;
import com.example.loanapi.dto.LoanRequestResponseDTO;
import com.example.loanapi.dto.PageRequest;
import com.example.loanapi.dto.PageResponse;
import com.example.loanapi.dto.UpdateLoanRequestDTO;
import com.example.loanapi.dto.UpdateLoanRequestStatusDTO;

/**
 * Service layer for loan request business logic
 */
public interface LoanRequestService {

    /**
     * Get all loan requests with pagination and filters
     * 
     * @param pageRequest pagination parameters
     * @param filter filter parameters
     * @return paginated response with loan requests
     */
    PageResponse<LoanRequestResponseDTO> getAllLoanRequests(PageRequest pageRequest, LoanRequestFilter filter);

    /**
     * Get a loan request by ID
     * 
     * @param id the loan request ID
     * @return the loan request as DTO
     * @throws com.example.loanapi.exception.LoanRequestNotFoundException if not found
     */
    LoanRequestResponseDTO getLoanRequestById(Long id);

    /**
     * Create a new loan request
     * 
     * @param createLoanRequestDTO the DTO with loan request data
     * @return the created loan request as DTO
     */
    LoanRequestResponseDTO createLoanRequest(CreateLoanRequestDTO createLoanRequestDTO);

    /**
     * Update an existing loan request
     * 
     * @param id the loan request ID
     * @param updateLoanRequestDTO the DTO with updated loan request data
     * @return the updated loan request as DTO
     * @throws com.example.loanapi.exception.LoanRequestNotFoundException if not found
     */
    LoanRequestResponseDTO updateLoanRequest(Long id, UpdateLoanRequestDTO updateLoanRequestDTO);

    /**
     * Update loan request status
     * 
     * @param id the loan request ID
     * @param updateStatusDTO the DTO with new status
     * @return the updated loan request as DTO
     * @throws com.example.loanapi.exception.LoanRequestNotFoundException if not found
     * @throws IllegalStateException if status transition is invalid
     */
    LoanRequestResponseDTO updateLoanRequestStatus(Long id, UpdateLoanRequestStatusDTO updateStatusDTO);

    /**
     * Delete a loan request
     * 
     * @param id the loan request ID
     * @throws com.example.loanapi.exception.LoanRequestNotFoundException if not found
     */
    void deleteLoanRequest(Long id);
}
