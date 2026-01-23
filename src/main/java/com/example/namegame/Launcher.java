package com.example.namegame;

/**
 * Launcher class for the application (needed for shaded JAR).
 * This class doesn't extend Application, allowing it to work with the maven-shade-plugin.
 */
public class Launcher {
    public static void main(String[] args) {
        NameGameApplication.main(args);
    }
}
