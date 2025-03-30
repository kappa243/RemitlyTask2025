package io.github.kappa243.remitly2025.model.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Size(min = 11, max = 11, message = "Invalid code length. SWIFT code must be 11 characters long")
@Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}[A-Z0-9]{3}$", message = "Invalid SWIFT code pattern")
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SwiftCode {
    String message() default "invalid SWIFT code";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}