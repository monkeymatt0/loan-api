package com.example.loanapi.aspect;

import com.example.loanapi.annotation.RequiresRole;
import com.example.loanapi.exception.ForbiddenException;
import com.example.loanapi.exception.UnauthorizedException;
import com.example.loanapi.model.LoanRequest;
import com.example.loanapi.model.User;
import com.example.loanapi.model.UserRole;
import com.example.loanapi.repository.LoanRequestRepository;
import com.example.loanapi.security.UserContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * AOP Aspect for role-based authorization
 * Intercepts methods annotated with @RequiresRole and @RequiresOwnership
 */
@Aspect
@Component
@Order(1)
public class AuthorizationAspect {

    private final LoanRequestRepository loanRequestRepository;

    @Autowired
    public AuthorizationAspect(LoanRequestRepository loanRequestRepository) {
        this.loanRequestRepository = loanRequestRepository;
    }

    @Before("@annotation(com.example.loanapi.annotation.RequiresRole)")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresRole requiresRole = method.getAnnotation(RequiresRole.class);

        User currentUser = UserContext.getCurrentUser();
        UserRole[] requiredRoles = requiresRole.value();

        boolean hasRequiredRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> role == currentUser.getRole());

        if (!hasRequiredRole) {
            throw new UnauthorizedException(
                String.format("User with role %s is not authorized. Required roles: %s",
                    currentUser.getRole(), Arrays.toString(requiredRoles)));
        }
    }

    @Before("@annotation(com.example.loanapi.annotation.RequiresOwnership)")
    public void checkOwnership(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // Extract the ID parameter (should be the first Long parameter)
        Long loanRequestId = null;
        Class<?>[] paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == Long.class || paramTypes[i] == long.class) {
                loanRequestId = (Long) args[i];
                break;
            }
        }

        if (loanRequestId == null) {
            throw new IllegalStateException("Could not find ID parameter in method " + method.getName());
        }

        // Make final for use in lambda
        final Long finalLoanRequestId = loanRequestId;

        // Get the loan request
        LoanRequest loanRequest = loanRequestRepository.findById(finalLoanRequestId)
                .orElseThrow(() -> new com.example.loanapi.exception.LoanRequestNotFoundException(finalLoanRequestId));

        // Get current user info
        User currentUser = UserContext.getCurrentUser();
        UserRole currentRole = currentUser.getRole();

        // GESTORE can always access
        if (currentRole == UserRole.GESTORE) {
            return;
        }

        // CLIENTE can only access their own requests
        if (currentRole == UserRole.CLIENTE) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (loanRequest.getUserId() == null || !loanRequest.getUserId().equals(currentUserId)) {
                throw new ForbiddenException(
                    String.format("User %d is not authorized to access loan request %d", currentUserId, finalLoanRequestId));
            }
            return;
        }

        // Other roles are not allowed
        throw new ForbiddenException("User role " + currentRole + " is not authorized to access loan requests");
    }
}
