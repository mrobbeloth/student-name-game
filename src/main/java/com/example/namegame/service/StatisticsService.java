package com.example.namegame.service;

import com.example.namegame.model.GameStatistics;
import com.example.namegame.model.Student;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages game statistics persistence.
 */
public class StatisticsService {
    private static final String STATISTICS_FILE = "statistics.json";
    
    private static StatisticsService instance;
    
    private final Path statisticsPath;
    private final Gson gson;
    private GameStatistics statistics;
    
    private StatisticsService() {
        this.statisticsPath = ConfigService.getInstance().getDataDirectory().resolve(STATISTICS_FILE);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }
    
    public static synchronized StatisticsService getInstance() {
        if (instance == null) {
            instance = new StatisticsService();
        }
        return instance;
    }
    
    private void load() {
        if (Files.exists(statisticsPath)) {
            try {
                String json = Files.readString(statisticsPath);
                statistics = gson.fromJson(json, GameStatistics.class);
                if (statistics == null) {
                    statistics = new GameStatistics();
                }
            } catch (IOException e) {
                System.err.println("Failed to load statistics: " + e.getMessage());
                statistics = new GameStatistics();
            }
        } else {
            statistics = new GameStatistics();
        }
    }
    
    private void save() {
        try {
            String json = gson.toJson(statistics);
            Files.writeString(statisticsPath, json);
        } catch (IOException e) {
            System.err.println("Failed to save statistics: " + e.getMessage());
        }
    }
    
    public GameStatistics getStatistics() {
        return statistics;
    }
    
    /**
     * Records an answer for a student.
     */
    public void recordAnswer(Student student, boolean correct) {
        statistics.recordAnswer(student, correct);
        save();
    }
    
    /**
     * Records that a game session was completed.
     */
    public void recordGameComplete(int bestStreak) {
        statistics.recordGameComplete(bestStreak);
        save();
    }
    
    /**
     * Resets all statistics.
     */
    public void reset() {
        statistics.reset();
        save();
    }
    
    /**
     * Gets lifetime accuracy percentage.
     */
    public double getLifetimeAccuracy() {
        return statistics.getOverallAccuracy();
    }
    
    /**
     * Gets total matches.
     */
    public int getTotalMatches() {
        return statistics.getTotalMatches();
    }
    
    /**
     * Gets total misses.
     */
    public int getTotalMisses() {
        return statistics.getTotalMisses();
    }
    
    /**
     * Gets games played.
     */
    public int getGamesPlayed() {
        return statistics.getGamesPlayed();
    }
    
    /**
     * Gets best streak.
     */
    public int getBestStreak() {
        return statistics.getBestStreak();
    }
    
    /**
     * Reloads statistics from disk.
     */
    public void reload() {
        load();
    }
}
