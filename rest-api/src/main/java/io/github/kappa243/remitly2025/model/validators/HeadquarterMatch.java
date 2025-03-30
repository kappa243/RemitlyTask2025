package io.github.kappa243.remitly2025.model.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = HeadquarterMatchValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HeadquarterMatch {
    String message() default "SWIFT code does not match headquarter status";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}