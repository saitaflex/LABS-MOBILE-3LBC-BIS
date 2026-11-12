package com.example.tpdm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ValidatorTest {

    @Test
    public void cin_mustBeEightLettersOrDigits() {
        assertTrue(Validator.isValidCin("12345678"));
        assertTrue(Validator.isValidCin("AB123456"));
        assertFalse(Validator.isValidCin("1234567"));
        assertFalse(Validator.isValidCin("123456789"));
        assertFalse(Validator.isValidCin("1234 678"));
        assertFalse(Validator.isValidCin(""));
        assertFalse(Validator.isValidCin(null));
    }

    @Test
    public void password_mustHaveTheMinimumLength() {
        assertTrue(Validator.isValidPassword("1234"));
        assertFalse(Validator.isValidPassword("123"));
        assertFalse(Validator.isValidPassword(null));
    }
}
