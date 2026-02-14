package com.pivovarit.utils;

public final class StringUtils {

    private StringUtils() {
    }

    public static String reverse(String input) {
        return input == null ? null : new StringBuilder(input).reverse().toString();
    }

    public static boolean isPalindrome(String input) {
        if (input == null) {
            return false;
        }
        String reversed = reverse(input);
        return input.equalsIgnoreCase(reversed);
    }
}
