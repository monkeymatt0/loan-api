package com.example.loanapi.repository;

import com.example.loanapi.model.LoanRequest;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for loan request persistence
 * Works with Model entities only
 */
public interface LoanRequestRepository {

    /**
     * Save a loan request (create or update)
     * 
     * @param loanRequest the loan request to save
     * @return the saved loan request
     */
    LoanRequest save(LoanRequest loanRequest);

    /**
     * Find a loan request by ID
     * 
     * @param id the loan request ID
     * @return Optional containing the loan request if found
     */
    Optional<LoanRequest> findById(Long id);

    /**
     * Find all loan requests
     * 
     * @return list of all loan requests
     */
    List<LoanRequest> findAll();

    /**
     * Delete a loan request by ID
     * 
     * @param id the loan request ID to delete
     */
    void deleteById(Long id);
}
