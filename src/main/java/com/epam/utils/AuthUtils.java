package com.epam.utils;

import java.security.SecureRandom;
import java.util.function.Predicate;

public final class AuthUtils {

    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHA_NUM = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private AuthUtils() {}

    public static String randomPassword(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHA_NUM.charAt(random.nextInt(ALPHA_NUM.length())));
        }
        return sb.toString();
    }

    public static String generateUsername(String first, String last, Predicate<String> exists) {
        String base = (first + "." + last).toLowerCase();
        String candidate = base;
        int suffix = 1;
        while (exists.test(candidate)) {
            candidate = base + "." + suffix++;
        }
        return candidate;
    }
}
