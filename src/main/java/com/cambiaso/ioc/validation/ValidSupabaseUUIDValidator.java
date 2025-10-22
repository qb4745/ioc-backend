package com.cambiaso.ioc.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

public class ValidSupabaseUUIDValidator implements ConstraintValidator<ValidSupabaseUUID, UUID> {
    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        // Allow null; @NotNull should handle requiredness
        if (value == null) return true;
        return value.toString().matches("^[0-9a-fA-F-]{36}$");
    }
}

