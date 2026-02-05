package com.example.loanapi.repository;

import com.example.loanapi.model.User;
import com.example.loanapi.model.UserRole;
import org.springframework.stereotype.Repository;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory implementation of UserRepository
 * Contains 2 predefined users for testing
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final List<User> users;

    public UserRepositoryImpl() {
        // Generate random tokens
        String clienteToken = generateRandomToken();
        String gestoreToken = generateRandomToken();
        
        // Create predefined users
        this.users = Arrays.asList(
            new User(1L, "Mario Rossi", "mario.rossi@example.com", UserRole.CLIENTE, clienteToken),
            new User(2L, "Luigi Bianchi", "luigi.bianchi@example.com", UserRole.GESTORE, gestoreToken)
        );
        
        // Log tokens for testing purposes
        System.out.println("=== Predefined Users ===");
        System.out.println("CLIENTE Token: " + clienteToken);
        System.out.println("GESTORE Token: " + gestoreToken);
        System.out.println("========================");
        
        // Save tokens to file
        saveTokensToFile(clienteToken, gestoreToken);
    }

    @Override
    public Optional<User> findByToken(String token) {
        return users.stream()
                .filter(user -> user.getToken().equals(token))
                .findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    /**
     * Generate a random token for user authentication
     * 
     * @return random token string
     */
    private String generateRandomToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Save tokens to a file in the project root
     * 
     * @param clienteToken the CLIENTE token
     * @param gestoreToken the GESTORE token
     */
    private void saveTokensToFile(String clienteToken, String gestoreToken) {
        String filePath = "tokens.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("=== Predefined Users ===");
            writer.println("CLIENTE Token: " + clienteToken);
            writer.println("GESTORE Token: " + gestoreToken);
            writer.println("========================");
            writer.println("Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (IOException e) {
            System.err.println("Warning: Failed to save tokens to file: " + e.getMessage());
        }
    }
}
