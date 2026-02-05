package com.example.loanapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

/**
 * Data Transfer Object for updating loan request status
 */
public class UpdateLoanRequestStatusDTO {

    @NotBlank(message = "Status is required")
    @Schema(
        example = "PENDIENTE", 
        description = "Stato del prestito",
        allowableValues = {"PENDIENTE", "APROBADA", "RECHAZADA", "CANCELADA"}
    )
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
