package com.example.tpdm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Local unit tests (run on the computer, no emulator needed). */
public class PasswordHasherTest {

    @Test
    public void hash_neverContainsThePassword() {
        String hash = PasswordHasher.hash("secret1234");
        assertFalse(hash.contains("secret1234"));
        assertTrue(PasswordHasher.isHashed(hash));
    }

    @Test
    public void verify_acceptsTheRightPassword() {
        assertTrue(PasswordHasher.verify("1234", PasswordHasher.hash("1234")));
    }

    @Test
    public void verify_rejectsAWrongPassword() {
        assertFalse(PasswordHasher.verify("12345", PasswordHasher.hash("1234")));
    }

    @Test
    public void hash_usesADifferentSaltEachTime() {
        assertNotEquals(PasswordHasher.hash("1234"), PasswordHasher.hash("1234"));
    }

    @Test
    public void verify_rejectsClearTextOrMalformedStoredValues() {
        assertFalse(PasswordHasher.verify("1234", "1234"));
        assertFalse(PasswordHasher.verify("1234", "zz:zz"));
        assertFalse(PasswordHasher.verify("1234", null));
        assertFalse(PasswordHasher.isHashed("1234"));
    }
}
