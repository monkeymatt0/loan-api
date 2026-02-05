package com.example.loanapi.controller;

import com.example.loanapi.annotation.RequiresOwnership;
import com.example.loanapi.annotation.RequiresRole;
import com.example.loanapi.dto.CreateLoanRequestDTO;
import com.example.loanapi.dto.LoanRequestFilter;
import com.example.loanapi.dto.LoanRequestResponseDTO;
import com.example.loanapi.dto.PageRequest;
import com.example.loanapi.dto.PageResponse;
import com.example.loanapi.dto.UpdateLoanRequestDTO;
import com.example.loanapi.dto.UpdateLoanRequestStatusDTO;
import com.example.loanapi.exception.LoanRequestNotFoundException;
import com.example.loanapi.model.UserRole;
import com.example.loanapi.service.LoanRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller for managing loan requests
 */
@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loan Requests", description = "API for managing personal loan requests")
@SecurityRequirement(name = "bearer-jwt")
public class LoanRequestController {

    private final LoanRequestService loanRequestService;

    @Autowired
    public LoanRequestController(LoanRequestService loanRequestService) {
        this.loanRequestService = loanRequestService;
    }

    /**
     * Get all loan requests with pagination and filters
     * 
     * @param page page number (default: 0)
     * @param size page size (default: 10)
     * @param status filter by status (optional)
     * @return paginated response with loan requests
     */
    @GetMapping
    @Operation(
        summary = "Get all loan requests",
        description = "Retrieve paginated list of loan requests with optional status filter"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved loan requests"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @RequiresRole({UserRole.CLIENTE, UserRole.GESTORE})
    public ResponseEntity<PageResponse<LoanRequestResponseDTO>> getAllLoanRequests(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "Filter by status (PENDIENTE, APROBADA, RECHAZADA, CANCELADA)", example = "PENDIENTE")
            @RequestParam(required = false) String status) {
        
        PageRequest pageRequest = new PageRequest(page, size);
        LoanRequestFilter filter = new LoanRequestFilter(status);
        
        PageResponse<LoanRequestResponseDTO> response = loanRequestService.getAllLoanRequests(pageRequest, filter);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a loan request by ID
     * 
     * @param id the loan request ID
     * @return the loan request
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get loan request by ID",
        description = "Retrieve a specific loan request by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved loan request"),
        @ApiResponse(responseCode = "404", description = "Loan request not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions or not owner")
    })
    @RequiresRole({UserRole.CLIENTE, UserRole.GESTORE})
    @RequiresOwnership
    public ResponseEntity<LoanRequestResponseDTO> getLoanRequestById(
            @Parameter(description = "Loan request ID", example = "1", required = true)
            @PathVariable Long id) {
        try {
            LoanRequestResponseDTO loanRequest = loanRequestService.getLoanRequestById(id);
            return ResponseEntity.ok(loanRequest);
        } catch (LoanRequestNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new loan request
     * 
     * @param createLoanRequestDTO the DTO with loan request data
     * @return the created loan request
     */
    @PostMapping
    @Operation(
        summary = "Create new loan request",
        description = "Create a new loan request. Only CLIENTE role can create loan requests."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Loan request created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation failed"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @RequiresRole(UserRole.CLIENTE)
    public ResponseEntity<LoanRequestResponseDTO> createLoanRequest(
            @Valid @RequestBody CreateLoanRequestDTO createLoanRequestDTO) {
        LoanRequestResponseDTO created = loanRequestService.createLoanRequest(createLoanRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing loan request
     * 
     * @param id the loan request ID
     * @param updateLoanRequestDTO the DTO with updated loan request data
     * @return the updated loan request
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update loan request",
        description = "Update an existing loan request"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan request updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - validation failed"),
        @ApiResponse(responseCode = "404", description = "Loan request not found")
    })
    @RequiresRole(UserRole.GESTORE)
    public ResponseEntity<LoanRequestResponseDTO> updateLoanRequest(
            @Parameter(description = "Loan request ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateLoanRequestDTO updateLoanRequestDTO) {
        try {
            LoanRequestResponseDTO updated = loanRequestService.updateLoanRequest(id, updateLoanRequestDTO);
            return ResponseEntity.ok(updated);
        } catch (LoanRequestNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update loan request status
     * 
     * @param id the loan request ID
     * @param updateStatusDTO the DTO with new status
     * @return the updated loan request
     */
    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update loan request status",
        description = "Update the status of a loan request. Only GESTORE role can update status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid status or validation failed"),
        @ApiResponse(responseCode = "404", description = "Loan request not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    @RequiresRole(UserRole.GESTORE)
    public ResponseEntity<LoanRequestResponseDTO> updateLoanRequestStatus(
            @Parameter(description = "Loan request ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateLoanRequestStatusDTO updateStatusDTO) {
        try {
            LoanRequestResponseDTO updated = loanRequestService.updateLoanRequestStatus(id, updateStatusDTO);
            return ResponseEntity.ok(updated);
        } catch (LoanRequestNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a loan request
     * 
     * @param id the loan request ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete loan request",
        description = "Delete a loan request by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Loan request deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Loan request not found")
    })
    public ResponseEntity<Void> deleteLoanRequest(
            @Parameter(description = "Loan request ID", example = "1", required = true)
            @PathVariable Long id) {
        try {
            loanRequestService.deleteLoanRequest(id);
            return ResponseEntity.noContent().build();
        } catch (LoanRequestNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
