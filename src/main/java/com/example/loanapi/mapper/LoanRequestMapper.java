package com.example.loanapi.mapper;

import com.example.loanapi.dto.CreateLoanRequestDTO;
import com.example.loanapi.dto.LoanRequestResponseDTO;
import com.example.loanapi.dto.UpdateLoanRequestDTO;
import com.example.loanapi.model.LoanRequest;

import java.time.LocalDateTime;

/**
 * Mapper for converting between DTOs and Model entities
 * No business logic, only field mapping
 */
public class LoanRequestMapper {

    /**
     * Convert CreateLoanRequestDTO to LoanRequest Model
     * Sets status to "Pendiente" and createdAt to current time
     * 
     * @param dto the create DTO
     * @param userId the user ID who creates the request
     * @return the LoanRequest model
     */
    public static LoanRequest toModel(CreateLoanRequestDTO dto, Long userId) {
        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setUserId(userId);
        loanRequest.setApplicantName(dto.getApplicantName());
        loanRequest.setAmount(dto.getAmount());
        loanRequest.setCurrency(dto.getCurrency());
        loanRequest.setIdentityDocument(dto.getIdentityDocument());
        loanRequest.setStatus("Pendiente");
        loanRequest.setCreatedAt(LocalDateTime.now());
        return loanRequest;
    }

    /**
     * Update existing LoanRequest with values from UpdateLoanRequestDTO
     * Preserves ID and createdAt, updates all other fields
     * 
     * @param dto the update DTO
     * @param existing the existing LoanRequest to update
     * @return the updated LoanRequest
     */
    public static LoanRequest toModel(UpdateLoanRequestDTO dto, LoanRequest existing) {
        existing.setApplicantName(dto.getApplicantName());
        existing.setAmount(dto.getAmount());
        existing.setCurrency(dto.getCurrency());
        existing.setIdentityDocument(dto.getIdentityDocument());
        // ID, status, and createdAt are not updated
        return existing;
    }

    /**
     * Convert LoanRequest Model to LoanRequestResponseDTO
     * 
     * @param loanRequest the LoanRequest model
     * @return the response DTO
     */
    public static LoanRequestResponseDTO toResponseDTO(LoanRequest loanRequest) {
        LoanRequestResponseDTO dto = new LoanRequestResponseDTO();
        dto.setId(loanRequest.getId());
        dto.setUserId(loanRequest.getUserId());
        dto.setApplicantName(loanRequest.getApplicantName());
        dto.setAmount(loanRequest.getAmount());
        dto.setCurrency(loanRequest.getCurrency());
        dto.setIdentityDocument(loanRequest.getIdentityDocument());
        dto.setStatus(loanRequest.getStatus());
        dto.setCreatedAt(loanRequest.getCreatedAt());
        return dto;
    }
}
