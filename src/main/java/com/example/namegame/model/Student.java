package com.example.namegame.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a student with their name and photo.
 */
public record Student(
    String firstName,
    String lastName,
    Path imagePath
) {
    public Student {
        Objects.requireNonNull(firstName, "First name is required");
        Objects.requireNonNull(lastName, "Last name is required");
        Objects.requireNonNull(imagePath, "Image path is required");
    }
    
    /**
     * Returns the display name in natural "First Last" format.
     */
    public String displayName() {
        return firstName + " " + lastName;
    }
    
    /**
     * Returns the name in roster format "Last, First".
     */
    public String rosterName() {
        return lastName + ", " + firstName;
    }
    
    /**
     * Returns a squashed version of the name for matching (e.g., "smithjohn").
     */
    public String squashedName() {
        return (lastName + firstName)
            .toLowerCase()
            .replaceAll("[\\s',.-]", "");
    }
}
