package com.cambiaso.ioc.validation;

import com.cambiaso.ioc.persistence.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UniqueEmailValidatorTest {

    private AppUserRepository appUserRepository;
    private UniqueEmailValidator validator;

    @BeforeEach
    void setUp() {
        appUserRepository = Mockito.mock(AppUserRepository.class);
        validator = new UniqueEmailValidator(appUserRepository);
    }

    @Test
    void returnsTrueForNullOrBlank() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("   ", null));
    }

    @Test
    void returnsFalseWhenEmailExists() {
        when(appUserRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);
        assertFalse(validator.isValid("ana@example.com", null));
        verify(appUserRepository, times(1)).existsByEmailIgnoreCase("ana@example.com");
    }

    @Test
    void returnsTrueWhenEmailNotExists() {
        when(appUserRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        assertTrue(validator.isValid("new@example.com", null));
        verify(appUserRepository, times(1)).existsByEmailIgnoreCase("new@example.com");
    }
}

