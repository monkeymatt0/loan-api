package com.example.loanapi.dto;

import com.example.loanapi.model.LoanStatus;

import javax.validation.constraints.Pattern;

/**
 * DTO for filtering loan requests
 */
public class LoanRequestFilter {

    @Pattern(regexp = "Pendiente|Aprobada|Rechazada|Cancelada", 
             message = "Status must be one of: Pendiente, Aprobada, Rechazada, Cancelada",
             flags = Pattern.Flag.CASE_INSENSITIVE)
    private String status;

    public LoanRequestFilter() {
    }

    public LoanRequestFilter(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Check if status filter is set
     * 
     * @return true if status is not null and not empty
     */
    public boolean hasStatusFilter() {
        return status != null && !status.trim().isEmpty();
    }

    /**
     * Get normalized status value
     * 
     * @return normalized status or null
     */
    public String getNormalizedStatus() {
        if (!hasStatusFilter()) {
            return null;
        }
        try {
            LoanStatus loanStatus = LoanStatus.fromString(status);
            return loanStatus.getValue();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
