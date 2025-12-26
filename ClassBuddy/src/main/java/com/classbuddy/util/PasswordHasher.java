package com.classbuddy.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {


    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }


    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }


    public static boolean isHashed(String password) {
        return password != null && password.startsWith("$2a$") || password.startsWith("$2b$");
    }
}