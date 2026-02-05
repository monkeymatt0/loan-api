package com.example.loanapi.annotation;

import com.example.loanapi.model.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify required roles for accessing a method
 * Used with AOP to enforce role-based authorization
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    /**
     * Array of roles required to access the method
     * 
     * @return array of UserRole
     */
    UserRole[] value();
}
