package com.skillbridge.careernavigator.util;

import java.util.regex.Pattern;

public class TextNormalizationUtils {

    /**
     * Formatting normalizer. Trims and handles spacing dynamically without destroying core programming characters (#, +).
     */
    public static String normalizeText(String input) {
        if (input == null) return "";
        // Removes punctuation except for #, +, and . which are critical to IT skills
        return input.toLowerCase()
                .replaceAll("[^a-z0-9#\\+\\.\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Creates a regex pattern for matching a skill name or alias with word boundaries.
     * Uses lookarounds to properly handle skills ending in special chars like C# or C++.
     */
    public static String buildBoundaryRegex(String target) {
        return "(?<![a-zA-Z0-9])" + Pattern.quote(target) + "(?![a-zA-Z0-9])";
    }
}
