package com.example.loanapi.repository;

import com.example.loanapi.model.User;

import java.util.Optional;

/**
 * Repository interface for user persistence
 */
public interface UserRepository {

    /**
     * Find a user by token
     * 
     * @param token the user token
     * @return Optional containing the user if found
     */
    Optional<User> findByToken(String token);

    /**
     * Find a user by ID
     * 
     * @param id the user ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(Long id);
}
