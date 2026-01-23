package com.example.namegame.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an active game session with score tracking.
 */
public class GameSession {
    private final GameMode mode;
    private final List<Student> students;
    private final List<Student> remainingStudents;
    private int score;
    private int misses;
    private int currentStreak;
    private int bestStreak;
    private Student currentStudent;
    
    public GameSession(GameMode mode, List<Student> students) {
        this.mode = mode;
        this.students = new ArrayList<>(students);
        this.remainingStudents = new ArrayList<>(students);
        Collections.shuffle(this.remainingStudents);
        this.score = 0;
        this.misses = 0;
        this.currentStreak = 0;
        this.bestStreak = 0;
        advanceToNext();
    }
    
    public GameMode getMode() {
        return mode;
    }
    
    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }
    
    public Student getCurrentStudent() {
        return currentStudent;
    }
    
    public int getScore() {
        return score;
    }
    
    public int getMisses() {
        return misses;
    }
    
    public int getCurrentStreak() {
        return currentStreak;
    }
    
    public int getBestStreak() {
        return bestStreak;
    }
    
    public int getTotalQuestions() {
        return students.size();
    }
    
    public int getQuestionsAnswered() {
        return score + misses;
    }
    
    public int getRemainingQuestions() {
        return remainingStudents.size() + (currentStudent != null ? 1 : 0);
    }
    
    public boolean isComplete() {
        return currentStudent == null && remainingStudents.isEmpty();
    }
    
    public double getAccuracy() {
        int total = score + misses;
        return total > 0 ? (double) score / total * 100 : 0;
    }
    
    /**
     * Records a correct answer.
     */
    public void recordCorrect() {
        score++;
        currentStreak++;
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak;
        }
        advanceToNext();
    }
    
    /**
     * Records an incorrect answer.
     */
    public void recordIncorrect() {
        misses++;
        currentStreak = 0;
        advanceToNext();
    }
    
    /**
     * Advances to the next student.
     */
    private void advanceToNext() {
        if (remainingStudents.isEmpty()) {
            currentStudent = null;
        } else {
            currentStudent = remainingStudents.remove(0);
        }
    }
    
    /**
     * Gets random distractors (wrong answers) for multiple choice.
     */
    public List<Student> getDistractors(int count) {
        List<Student> available = new ArrayList<>(students);
        available.remove(currentStudent);
        Collections.shuffle(available);
        return available.subList(0, Math.min(count, available.size()));
    }
}
