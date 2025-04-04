package io.github.kappa243.remitly2025.model.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Size(min = 2, max = 2, message = "Invalid code length. Country code must be 2 characters long")
@Uppercase
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CountryCode {
    String message() default "invalid country code";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}