package com.classbuddy.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    /**
     * Hash a plain text password using BCrypt
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Verify if a plain password matches a hashed password
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /**
     * Check if a string is already hashed (basic check)
     */
    public static boolean isHashed(String password) {
        return password != null && password.startsWith("$2a$") || password.startsWith("$2b$");
    }
}