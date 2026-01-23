package com.example.namegame.model;

/**
 * Enum representing the available game modes.
 */
public enum GameMode {
    MATCHING("Matching", "Match photos to names", 1),
    MULTIPLE_CHOICE("Multiple Choice", "Pick the correct name from 4 choices", 4),
    FILL_IN_BLANK("Fill in the Blank", "Type the student's name", 1);
    
    private final String displayName;
    private final String description;
    private final int minimumStudents;
    
    GameMode(String displayName, String description, int minimumStudents) {
        this.displayName = displayName;
        this.description = description;
        this.minimumStudents = minimumStudents;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getMinimumStudents() {
        return minimumStudents;
    }
    
    /**
     * Checks if this game mode can be played with the given number of students.
     */
    public boolean canPlayWith(int studentCount) {
        return studentCount >= minimumStudents;
    }
}
