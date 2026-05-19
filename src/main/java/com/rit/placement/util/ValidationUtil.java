package com.rit.placement.util;

import java.util.regex.Pattern;

/**
 * Central validation utility for standardizing inputs and preventing injection.
 */
public class ValidationUtil {

    private static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.,]*$");
    private static final Pattern USN_PATTERN = Pattern.compile("^[1-4][A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{3}$");
    
    public static String sanitizeString(String input, int maxLength) {
        if (input == null) return "";
        input = input.trim();
        if (input.length() > maxLength) {
            input = input.substring(0, maxLength);
        }
        // Basic sanitization - remove control characters
        input = input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        return input;
    }

    public static boolean isValidBranch(String branch) {
        if (branch == null || branch.trim().isEmpty()) return false;
        String b = branch.trim().toUpperCase();
        // Allow common branches or check against a defined list
        return b.matches("^[A-Z]{2,4}$"); // e.g. CSE, ISE, ECE
    }
    
    public static boolean isAlphaNumericWithPunctuation(String input) {
        if (input == null) return true;
        return ALPHA_NUMERIC_PATTERN.matcher(input).matches();
    }
    
    public static boolean isValidUsn(String usn) {
        if (usn == null) return false;
        return USN_PATTERN.matcher(usn.trim().toUpperCase()).matches();
    }
    
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
