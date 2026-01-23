package com.example.namegame.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Represents an image file that couldn't be automatically matched to a roster entry.
 */
public record UnmatchedImage(
    Path path,
    List<ScoredMatch> suggestions
) {
    public UnmatchedImage {
        Objects.requireNonNull(path, "Path is required");
        suggestions = suggestions != null ? List.copyOf(suggestions) : List.of();
    }
    
    /**
     * Returns the filename without path.
     */
    public String filename() {
        return path.getFileName().toString();
    }
    
    /**
     * Returns the best suggestion if available.
     */
    public ScoredMatch bestSuggestion() {
        return suggestions.isEmpty() ? null : suggestions.get(0);
    }
    
    /**
     * Returns true if there's a strong match suggestion.
     */
    public boolean hasStrongSuggestion() {
        ScoredMatch best = bestSuggestion();
        return best != null && best.isStrongMatch();
    }
}
