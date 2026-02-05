package com.example.loanapi.model;

/**
 * Enum representing loan request status values
 */
public enum LoanStatus {
    PENDIENTE("Pendiente"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    CANCELADA("Cancelada");

    private final String value;

    LoanStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Convert string to LoanStatus enum
     * 
     * @param status the status string
     * @return the corresponding LoanStatus enum
     * @throws IllegalArgumentException if status string is invalid
     */
    public static LoanStatus fromString(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        for (LoanStatus loanStatus : LoanStatus.values()) {
            if (loanStatus.value.equalsIgnoreCase(status)) {
                return loanStatus;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + status);
    }
}
