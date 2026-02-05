package com.example.loanapi.model;

/**
 * Enum representing user roles in the system
 */
public enum UserRole {
    CLIENTE,
    GESTORE;

    /**
     * Convert string to UserRole enum
     * 
     * @param role the role string
     * @return the corresponding UserRole enum
     * @throws IllegalArgumentException if role string is invalid
     */
    public static UserRole fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + role);
    }
}
