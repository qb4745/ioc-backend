package com.cambiaso.ioc.validation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValidSupabaseUUIDValidatorTest {

    private final ValidSupabaseUUIDValidator validator = new ValidSupabaseUUIDValidator();

    @Test
    void returnsTrueForNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void returnsTrueForValidUUID() {
        UUID id = UUID.randomUUID();
        assertTrue(validator.isValid(id, null));
    }
}

