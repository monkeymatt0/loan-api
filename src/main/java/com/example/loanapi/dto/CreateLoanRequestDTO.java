package com.example.loanapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating a loan request
 */
public class CreateLoanRequestDTO {

    @NotBlank(message = "Applicant name is required")
    @Schema(example = "Mario Rossi", description = "Nome completo del richiedente")
    private String applicantName;

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be positive")
    @Min(value = 1, message = "Minimum loan amount is 1")
    @Schema(example = "5000.00", description = "Importo del prestito")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(EUR|USD)$", message = "Currency must be EUR or USD")
    @Schema(example = "EUR", description = "Valuta (EUR o USD)", allowableValues = {"EUR", "USD"})
    private String currency;

    @NotBlank(message = "Identity document is required")
    @Pattern(regexp = "^[A-Za-z]{3}[0-9]{5}$", message = "Identity document must be 3 letters followed by 5 numbers (e.g., ABC12345)")
    @Schema(example = "ABC12345", description = "Documento di identità (3 lettere seguite da 5 numeri, es. ABC12345)")
    private String identityDocument;


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
}
