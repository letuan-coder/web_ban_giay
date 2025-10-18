package com.example.DATN.Validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {HexCodeValidator.class})
public @interface HexCodeConstraint {
    String message() default "INVALID_HEX_CODE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
