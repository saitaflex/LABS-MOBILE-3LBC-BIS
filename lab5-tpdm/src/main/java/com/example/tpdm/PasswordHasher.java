package com.example.tpdm;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Turns a password into a salted PBKDF2 hash so the database never stores the real password.
 * Stored format: "saltHex:hashHex". The salt is random, so two users with the same
 * password get two different hashes.
 */
final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 10_000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final String SEPARATOR = ":";

    private PasswordHasher() {
    }

    static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return toHex(salt) + SEPARATOR + toHex(derive(password, salt));
    }

    /** @return true if the password matches the stored "salt:hash" value. */
    static boolean verify(String password, String stored) {
        if (password == null || !isHashed(stored)) {
            return false;
        }
        String[] parts = stored.split(SEPARATOR);
        byte[] expected = fromHex(parts[1]);
        byte[] actual = derive(password, fromHex(parts[0]));
        // Constant-time comparison.
        return MessageDigest.isEqual(expected, actual);
    }

    /** @return true if the value already has the "saltHex:hashHex" shape. */
    static boolean isHashed(String stored) {
        return stored != null
                && stored.matches("[0-9a-f]{" + (SALT_BYTES * 2) + "}:[0-9a-f]{" + (KEY_BITS / 4) + "}");
    }

    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            // PBKDF2WithHmacSHA1 exists on every Android version; reaching this is a bug.
            throw new IllegalStateException("Password hashing is unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return out;
    }
}
