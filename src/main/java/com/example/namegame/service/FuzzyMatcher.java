package com.example.namegame.service;

import com.example.namegame.model.ScoredMatch;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides fuzzy string matching using Levenshtein distance.
 */
public class FuzzyMatcher {
    private static final int MAX_SUGGESTIONS = 5;
    private static final int STRONG_MATCH_THRESHOLD = 3;
    
    private final LevenshteinDistance levenshtein;
    
    public FuzzyMatcher() {
        this.levenshtein = LevenshteinDistance.getDefaultInstance();
    }
    
    /**
     * Finds the closest matches for a squashed name.
     * @param squashedFilename The squashed name from the filename (e.g., "smithjohn")
     * @param squashedToOriginal Map of squashed roster names to original roster names
     * @return List of scored matches, sorted by distance (best first)
     */
    public List<ScoredMatch> findMatches(String squashedFilename, Map<String, String> squashedToOriginal) {
        String target = squashedFilename.toLowerCase();
        
        return squashedToOriginal.entrySet().stream()
            .map(entry -> {
                int distance = levenshtein.apply(target, entry.getKey());
                return new ScoredMatch(entry.getValue(), distance);
            })
            .sorted()
            .limit(MAX_SUGGESTIONS)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds the best match if it's within the threshold.
     * @param squashedFilename The squashed name from the filename
     * @param squashedToOriginal Map of squashed roster names to original roster names
     * @return The best match roster name, or null if no strong match
     */
    public String findBestMatch(String squashedFilename, Map<String, String> squashedToOriginal) {
        List<ScoredMatch> matches = findMatches(squashedFilename, squashedToOriginal);
        
        if (!matches.isEmpty() && matches.get(0).isStrongMatch()) {
            return matches.get(0).rosterName();
        }
        
        return null;
    }
    
    /**
     * Squashes a roster name for comparison.
     * "Smith, John" -> "smithjohn"
     */
    public static String squashRosterName(String rosterName) {
        if (rosterName == null || rosterName.isEmpty()) {
            return "";
        }
        
        // Split on comma: "Smith, John" -> ["Smith", " John"]
        String[] parts = rosterName.split(",", 2);
        
        String lastName = parts[0].trim();
        String firstName = parts.length > 1 ? parts[1].trim() : "";
        
        // Combine and normalize
        return (lastName + firstName)
            .toLowerCase()
            .replaceAll("[\\s',.-]", "");
    }
    
    /**
     * Extracts the squashed name from an image filename.
     * "SmithJohn_12345.jpg" -> "smithjohn"
     */
    public static String extractNameFromFilename(String filename) {
        // Remove extension
        int dotIndex = filename.lastIndexOf('.');
        String withoutExt = dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
        
        // Split on underscore, take first part
        int underscoreIndex = withoutExt.indexOf('_');
        String namePart = underscoreIndex > 0 ? withoutExt.substring(0, underscoreIndex) : withoutExt;
        
        // Normalize
        return namePart.toLowerCase().replaceAll("[',.-]", "");
    }
    
    /**
     * Checks if a user input matches a student name (flexible matching).
     * Accepts "First Last", "Last, First", case-insensitive, ignoring punctuation.
     */
    public static boolean isNameMatch(String input, String firstName, String lastName) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        String normalizedInput = input.trim().toLowerCase().replaceAll("[,.]", " ").replaceAll("\\s+", " ");
        String firstLast = (firstName + " " + lastName).toLowerCase();
        String lastFirst = (lastName + " " + firstName).toLowerCase();
        
        return normalizedInput.equals(firstLast) || normalizedInput.equals(lastFirst);
    }
}
