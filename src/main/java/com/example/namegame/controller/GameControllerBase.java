package com.example.namegame.controller;

import com.example.namegame.model.GameMode;
import com.example.namegame.model.GameSession;
import com.example.namegame.model.Student;
import com.example.namegame.service.ImageService;
import com.example.namegame.service.SoundService;
import com.example.namegame.service.StatisticsService;
import javafx.stage.Stage;

import java.util.List;

/**
 * Base class for game controllers.
 */
public abstract class GameControllerBase {
    
    protected Stage stage;
    protected GameMode mode;
    protected GameSession session;
    protected Runnable onGameComplete;
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setMode(GameMode mode) {
        this.mode = mode;
        initializeGame();
    }
    
    public void setOnGameComplete(Runnable callback) {
        this.onGameComplete = callback;
    }
    
    protected void initializeGame() {
        List<Student> students = ImageService.getInstance().getStudents();
        session = new GameSession(mode, students);
        loadQuestion();
    }
    
    protected abstract void loadQuestion();
    
    protected void handleCorrectAnswer() {
        SoundService.getInstance().playCorrect();
        StatisticsService.getInstance().recordAnswer(session.getCurrentStudent(), true);
        
        // Check for streak bonus
        session.recordCorrect();
        if (session.getCurrentStreak() > 0 && session.getCurrentStreak() % 5 == 0) {
            SoundService.getInstance().playStreak();
            onStreakBonus();
        }
        
        playCorrectAnimation();
        
        // Delay before next question
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(800));
        pause.setOnFinished(e -> {
            if (session.isComplete()) {
                endGame();
            } else {
                loadQuestion();
            }
        });
        pause.play();
    }
    
    protected void handleIncorrectAnswer() {
        SoundService.getInstance().playIncorrect();
        StatisticsService.getInstance().recordAnswer(session.getCurrentStudent(), false);
        session.recordIncorrect();
        
        playIncorrectAnimation();
        showCorrectAnswer();
        
        // Longer delay to show correct answer
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
        pause.setOnFinished(e -> {
            if (session.isComplete()) {
                endGame();
            } else {
                loadQuestion();
            }
        });
        pause.play();
    }
    
    protected void endGame() {
        SoundService.getInstance().playComplete();
        StatisticsService.getInstance().recordGameComplete(session.getBestStreak());
        showResults();
    }
    
    protected abstract void playCorrectAnimation();
    protected abstract void playIncorrectAnimation();
    protected abstract void showCorrectAnswer();
    protected abstract void onStreakBonus();
    protected abstract void showResults();
    
    protected void returnHome() {
        if (onGameComplete != null) {
            onGameComplete.run();
        }
    }
    
    protected void updateScoreDisplay() {
        // Override in subclasses
    }
}
