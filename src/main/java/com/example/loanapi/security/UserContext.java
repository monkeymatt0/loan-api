package com.example.loanapi.security;

import com.example.loanapi.model.User;
import com.example.loanapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class to access current user information from HTTP request
 */
@Component
public class UserContext {

    private static UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        UserContext.userRepository = userRepository;
    }

    /**
     * Get the current user from the Authorization Bearer token
     * 
     * @return the current User
     * @throws com.example.loanapi.exception.UnauthorizedException if token is missing or invalid
     */
    public static User getCurrentUser() {
        String token = extractTokenFromRequest();
        if (token == null || token.isEmpty()) {
            throw new com.example.loanapi.exception.UnauthorizedException("Authorization token is required");
        }
        
        return userRepository.findByToken(token)
                .orElseThrow(() -> new com.example.loanapi.exception.UnauthorizedException("Invalid or expired token"));
    }

    /**
     * Get the current user ID
     * 
     * @return the current user ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Get the current user role
     * 
     * @return the current user role
     */
    public static com.example.loanapi.model.UserRole getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    /**
     * Extract Bearer token from Authorization header
     * 
     * @return the token string or null if not found
     */
    private static String extractTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        return authHeader.substring(7); // Remove "Bearer " prefix
    }
}
