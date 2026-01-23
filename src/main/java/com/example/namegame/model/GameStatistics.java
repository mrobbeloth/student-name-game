package com.example.namegame.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds lifetime statistics for the game.
 */
public class GameStatistics {
    private int totalMatches;
    private int totalMisses;
    private int gamesPlayed;
    private int bestStreak;
    private Map<String, StudentStats> perStudentStats;
    
    public GameStatistics() {
        this.totalMatches = 0;
        this.totalMisses = 0;
        this.gamesPlayed = 0;
        this.bestStreak = 0;
        this.perStudentStats = new HashMap<>();
    }
    
    public int getTotalMatches() {
        return totalMatches;
    }
    
    public void setTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
    }
    
    public int getTotalMisses() {
        return totalMisses;
    }
    
    public void setTotalMisses(int totalMisses) {
        this.totalMisses = totalMisses;
    }
    
    public int getGamesPlayed() {
        return gamesPlayed;
    }
    
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }
    
    public int getBestStreak() {
        return bestStreak;
    }
    
    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }
    
    public Map<String, StudentStats> getPerStudentStats() {
        return perStudentStats;
    }
    
    public void setPerStudentStats(Map<String, StudentStats> perStudentStats) {
        this.perStudentStats = perStudentStats;
    }
    
    public double getOverallAccuracy() {
        int total = totalMatches + totalMisses;
        return total > 0 ? (double) totalMatches / total * 100 : 0;
    }
    
    public void recordAnswer(Student student, boolean correct) {
        if (correct) {
            totalMatches++;
        } else {
            totalMisses++;
        }
        
        String key = student.squashedName();
        StudentStats stats = perStudentStats.computeIfAbsent(key, k -> new StudentStats());
        if (correct) {
            stats.incrementCorrect();
        } else {
            stats.incrementIncorrect();
        }
    }
    
    public void recordGameComplete(int streak) {
        gamesPlayed++;
        if (streak > bestStreak) {
            bestStreak = streak;
        }
    }
    
    public void reset() {
        totalMatches = 0;
        totalMisses = 0;
        gamesPlayed = 0;
        bestStreak = 0;
        perStudentStats.clear();
    }
    
    /**
     * Statistics for an individual student.
     */
    public static class StudentStats {
        private int correct;
        private int incorrect;
        
        public StudentStats() {
            this.correct = 0;
            this.incorrect = 0;
        }
        
        public int getCorrect() {
            return correct;
        }
        
        public void setCorrect(int correct) {
            this.correct = correct;
        }
        
        public int getIncorrect() {
            return incorrect;
        }
        
        public void setIncorrect(int incorrect) {
            this.incorrect = incorrect;
        }
        
        public void incrementCorrect() {
            correct++;
        }
        
        public void incrementIncorrect() {
            incorrect++;
        }
        
        public double getAccuracy() {
            int total = correct + incorrect;
            return total > 0 ? (double) correct / total * 100 : 0;
        }
    }
}
