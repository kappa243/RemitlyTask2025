package io.github.kappa243.remitly2025.model.validators;

import io.github.kappa243.remitly2025.controllers.BankRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HeadquarterMatchValidator implements ConstraintValidator<HeadquarterMatch, BankRequest> {
    @Override
    public void initialize(HeadquarterMatch constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }
    
    @Override
    public boolean isValid(BankRequest value, ConstraintValidatorContext context) {
        return value.isHeadquarter() == value.getSwiftCode().startsWith("XXX", 8);
    }
}
