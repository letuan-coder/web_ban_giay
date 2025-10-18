package com.example.DATN.Validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class HexCodeValidator implements ConstraintValidator<HexCodeConstraint, String> {
    private static final String HEX_PATTERN = "#?([\\da-fA-F]{2})([\\da-fA-F]{2})([\\da-fA-F]{2})";
    @Override
    public void initialize(HexCodeConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false; // hoặc true nếu bạn muốn cho phép null
        }
        return value.matches(HEX_PATTERN);
    }
}
