package com.example.loanapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for loan request response
 */
public class LoanRequestResponseDTO {

    @Schema(example = "1", description = "ID del prestito")
    private Long id;
    
    @Schema(example = "1", description = "ID dell'utente")
    private Long userId;
    
    @Schema(example = "Mario Rossi", description = "Nome completo del richiedente")
    private String applicantName;
    
    @Schema(example = "5000.00", description = "Importo del prestito")
    private BigDecimal amount;
    
    @Schema(example = "EUR", description = "Valuta")
    private String currency;
    
    @Schema(example = "AB123456", description = "Documento di identità")
    private String identityDocument;
    
    @Schema(example = "PENDIENTE", description = "Stato del prestito")
    private String status;
    
    @Schema(example = "2026-02-04T10:30:00", description = "Data e ora di creazione")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getIdentityDocument() {
        return identityDocument;
    }

    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
