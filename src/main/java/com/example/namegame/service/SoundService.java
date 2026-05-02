package com.example.namegame.service;

import com.example.namegame.util.AppLogger;
import javafx.scene.media.AudioClip;

import java.net.URL;

/**
 * Manages sound effects for the game.
 */
public class SoundService {
    private static SoundService instance;
    
    private AudioClip correctSound;
    private AudioClip incorrectSound;
    private AudioClip streakSound;
    private AudioClip completeSound;
    private boolean enabled;
    
    private SoundService() {
        this.enabled = true;
        loadSounds();
    }
    
    public static synchronized SoundService getInstance() {
        if (instance == null) {
            instance = new SoundService();
        }
        return instance;
    }
    
    private void loadSounds() {
        correctSound = loadSound("/sounds/correct.wav");
        incorrectSound = loadSound("/sounds/incorrect.wav");
        streakSound = loadSound("/sounds/streak.wav");
        completeSound = loadSound("/sounds/complete.wav");
    }
    
    private AudioClip loadSound(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                return new AudioClip(url.toExternalForm());
            } else {
                AppLogger.log("Sound not found: " + resourcePath);
                return null;
            }
        } catch (Exception e) {
            AppLogger.log("Failed to load sound " + resourcePath, e);
            return null;
        }
    }
    
    public void playCorrect() {
        playSound(correctSound);
    }
    
    public void playIncorrect() {
        playSound(incorrectSound);
    }
    
    public void playStreak() {
        playSound(streakSound);
    }
    
    public void playComplete() {
        playSound(completeSound);
    }
    
    private void playSound(AudioClip clip) {
        if (enabled && clip != null) {
            try {
                clip.play();
            } catch (Exception e) {
                AppLogger.log("Error playing sound", e);
            }
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }
}
