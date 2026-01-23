package com.example.namegame.controller;

import com.example.namegame.model.Student;
import com.example.namegame.util.AnimationFactory;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.util.*;

/**
 * Controller for the matching game mode.
 */
public class MatchingGameController extends GameControllerBase {
    
    @FXML private FlowPane imagePane;
    @FXML private FlowPane namePane;
    @FXML private Label scoreLabel;
    @FXML private Label progressLabel;
    @FXML private Label streakLabel;
    @FXML private Button homeButton;
    
    private final Map<Button, Student> imageButtons = new HashMap<>();
    private final Map<Button, Student> nameButtons = new HashMap<>();
    private Button selectedImageButton = null;
    private Button selectedNameButton = null;
    private int matchesFound = 0;
    
    @FXML
    public void initialize() {
        // Will be called after FXML loads
    }
    
    @Override
    protected void initializeGame() {
        super.initializeGame();
        setupMatchingGrid();
    }
    
    private void setupMatchingGrid() {
        imagePane.getChildren().clear();
        namePane.getChildren().clear();
        imageButtons.clear();
        nameButtons.clear();
        matchesFound = 0;
        
        List<Student> students = new ArrayList<>(session.getStudents());
        Collections.shuffle(students);
        
        // Create image buttons
        for (Student student : students) {
            Button btn = createImageButton(student);
            imageButtons.put(btn, student);
            imagePane.getChildren().add(btn);
        }
        
        // Shuffle again for names
        List<Student> shuffledForNames = new ArrayList<>(students);
        Collections.shuffle(shuffledForNames);
        
        // Create name buttons
        for (Student student : shuffledForNames) {
            Button btn = createNameButton(student);
            nameButtons.put(btn, student);
            namePane.getChildren().add(btn);
        }
        
        updateScoreDisplay();
    }
    
    private Button createImageButton(Student student) {
        Button btn = new Button();
        btn.setPrefSize(120, 140);
        btn.setMinSize(120, 140);
        btn.getStyleClass().add("image-card");
        
        try {
            Image image = new Image(new FileInputStream(student.imagePath().toFile()), 100, 120, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(100);
            imageView.setFitHeight(120);
            imageView.setPreserveRatio(true);
            btn.setGraphic(imageView);
        } catch (Exception e) {
            btn.setText("?");
        }
        
        btn.setOnAction(e -> selectImage(btn));
        return btn;
    }
    
    private Button createNameButton(Student student) {
        Button btn = new Button(student.displayName());
        btn.setPrefSize(150, 40);
        btn.setMinWidth(150);
        btn.getStyleClass().add("name-card");
        btn.setWrapText(true);
        
        btn.setOnAction(e -> selectName(btn));
        return btn;
    }
    
    private void selectImage(Button btn) {
        if (selectedImageButton != null) {
            selectedImageButton.getStyleClass().remove("selected");
        }
        selectedImageButton = btn;
        btn.getStyleClass().add("selected");
        
        checkMatch();
    }
    
    private void selectName(Button btn) {
        if (selectedNameButton != null) {
            selectedNameButton.getStyleClass().remove("selected");
        }
        selectedNameButton = btn;
        btn.getStyleClass().add("selected");
        
        checkMatch();
    }
    
    private void checkMatch() {
        if (selectedImageButton == null || selectedNameButton == null) {
            return;
        }
        
        Student imageStudent = imageButtons.get(selectedImageButton);
        Student nameStudent = nameButtons.get(selectedNameButton);
        
        if (imageStudent.equals(nameStudent)) {
            // Correct match!
            handleCorrectMatch();
        } else {
            // Incorrect match
            handleIncorrectMatch();
        }
    }
    
    private void handleCorrectMatch() {
        Button imgBtn = selectedImageButton;
        Button nameBtn = selectedNameButton;
        
        AnimationFactory.playCorrect(imgBtn);
        AnimationFactory.playCorrect(nameBtn);
        
        session.recordCorrect();
        matchesFound++;
        
        // Disable matched buttons after animation
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
        pause.setOnFinished(e -> {
            imgBtn.setDisable(true);
            imgBtn.setOpacity(0.3);
            nameBtn.setDisable(true);
            nameBtn.setOpacity(0.3);
            
            selectedImageButton = null;
            selectedNameButton = null;
            
            updateScoreDisplay();
            
            if (matchesFound >= session.getStudents().size()) {
                endGame();
            }
        });
        pause.play();
        
        // Check for streak
        if (session.getCurrentStreak() > 0 && session.getCurrentStreak() % 5 == 0) {
            onStreakBonus();
        }
    }
    
    private void handleIncorrectMatch() {
        AnimationFactory.playIncorrect(selectedImageButton);
        AnimationFactory.playIncorrect(selectedNameButton);
        
        session.recordIncorrect();
        
        Button imgBtn = selectedImageButton;
        Button nameBtn = selectedNameButton;
        
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
        pause.setOnFinished(e -> {
            imgBtn.getStyleClass().remove("selected");
            nameBtn.getStyleClass().remove("selected");
            selectedImageButton = null;
            selectedNameButton = null;
            updateScoreDisplay();
        });
        pause.play();
    }
    
    @Override
    protected void loadQuestion() {
        // Not used in matching mode - all questions shown at once
    }
    
    @Override
    protected void playCorrectAnimation() {
        // Handled in handleCorrectMatch
    }
    
    @Override
    protected void playIncorrectAnimation() {
        // Handled in handleIncorrectMatch
    }
    
    @Override
    protected void showCorrectAnswer() {
        // Not applicable for matching
    }
    
    @Override
    protected void onStreakBonus() {
        if (streakLabel != null) {
            streakLabel.setText("🔥 " + session.getCurrentStreak() + " streak!");
            AnimationFactory.playStreakBonus(streakLabel);
        }
    }
    
    @Override
    protected void showResults() {
        Alert results = new Alert(Alert.AlertType.INFORMATION);
        results.setTitle("Game Complete!");
        results.setHeaderText("Matching Complete!");
        results.setContentText(String.format("""
            Score: %d / %d
            Accuracy: %.1f%%
            Best Streak: %d
            """,
            session.getScore(),
            session.getTotalQuestions(),
            session.getAccuracy(),
            session.getBestStreak()
        ));
        
        results.showAndWait();
        returnHome();
    }
    
    @Override
    protected void updateScoreDisplay() {
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + session.getScore() + " / " + session.getTotalQuestions());
        }
        if (progressLabel != null) {
            progressLabel.setText("Matched: " + matchesFound + " / " + session.getStudents().size());
        }
        if (streakLabel != null && session.getCurrentStreak() > 0) {
            streakLabel.setText("🔥 " + session.getCurrentStreak());
        }
    }
    
    @FXML
    private void goHome() {
        returnHome();
    }
}
