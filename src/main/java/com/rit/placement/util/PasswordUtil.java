package com.rit.placement.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for password hashing using BCrypt.
 */
public class PasswordUtil {

    private static final int BCRYPT_COST = 12;

    /**
     * Hashes a plain-text password using BCrypt.
     *
     * @param plainPassword the raw password entered by the user
     * @return BCrypt hash ready for storage
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password must not be blank.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainPassword the password entered during login
     * @param storedHash    the BCrypt hash retrieved from the database
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || !isBCryptHash(storedHash)) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, storedHash);
    }

    public static boolean isBCryptHash(String storedHash) {
        return storedHash != null
                && (storedHash.startsWith("$2a$")
                || storedHash.startsWith("$2b$")
                || storedHash.startsWith("$2y$"));
    }
}
