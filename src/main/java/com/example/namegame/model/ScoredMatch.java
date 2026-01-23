package com.example.namegame.model;

/**
 * Represents a fuzzy match result with the roster name and edit distance.
 */
public record ScoredMatch(
    String rosterName,
    int distance
) implements Comparable<ScoredMatch> {
    
    @Override
    public int compareTo(ScoredMatch other) {
        return Integer.compare(this.distance, other.distance);
    }
    
    /**
     * Returns true if this is a strong match (distance <= 3).
     */
    public boolean isStrongMatch() {
        return distance <= 3;
    }
}
