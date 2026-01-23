package com.example.namegame.controller;

import com.example.namegame.model.Student;
import com.example.namegame.service.KeyboardShortcutService;
import com.example.namegame.util.AnimationFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.*;

/**
 * Controller for the multiple choice game mode.
 */
public class MultipleChoiceController extends GameControllerBase {
    
    @FXML private ImageView studentImage;
    @FXML private Button choice1;
    @FXML private Button choice2;
    @FXML private Button choice3;
    @FXML private Button choice4;
    @FXML private Label scoreLabel;
    @FXML private Label progressLabel;
    @FXML private Label streakLabel;
    @FXML private Label feedbackLabel;
    @FXML private VBox rootPane;
    
    private List<Button> choiceButtons;
    private int correctIndex;
    private boolean answered = false;
    
    @FXML
    public void initialize() {
        choiceButtons = List.of(choice1, choice2, choice3, choice4);
        
        // Set up button actions
        for (int i = 0; i < choiceButtons.size(); i++) {
            final int index = i;
            choiceButtons.get(i).setOnAction(e -> selectAnswer(index));
        }
    }
    
    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
        setupKeyboardShortcuts();
    }
    
    private void setupKeyboardShortcuts() {
        stage.getScene().setOnKeyPressed(event -> {
            if (answered) return;
            
            KeyCode code = event.getCode();
            if (code == KeyboardShortcutService.CHOICE_1) {
                selectAnswer(0);
            } else if (code == KeyboardShortcutService.CHOICE_2) {
                selectAnswer(1);
            } else if (code == KeyboardShortcutService.CHOICE_3) {
                selectAnswer(2);
            } else if (code == KeyboardShortcutService.CHOICE_4) {
                selectAnswer(3);
            }
        });
    }
    
    @Override
    protected void loadQuestion() {
        answered = false;
        feedbackLabel.setText("");
        feedbackLabel.setVisible(false);
        
        Student current = session.getCurrentStudent();
        if (current == null) {
            endGame();
            return;
        }
        
        // Load image
        try {
            Image image = new Image(new FileInputStream(current.imagePath().toFile()), 300, 400, true, true);
            studentImage.setImage(image);
            AnimationFactory.slideInFromRight(studentImage);
        } catch (Exception e) {
            studentImage.setImage(null);
        }
        
        // Get distractors (wrong answers)
        List<Student> distractors = session.getDistractors(3);
        
        // Create choices list with correct answer
        List<Student> choices = new ArrayList<>();
        choices.add(current); // Correct answer
        choices.addAll(distractors);
        Collections.shuffle(choices);
        
        // Find correct index
        correctIndex = choices.indexOf(current);
        
        // Set button texts
        for (int i = 0; i < choiceButtons.size(); i++) {
            Button btn = choiceButtons.get(i);
            btn.setText((i + 1) + ". " + choices.get(i).displayName());
            btn.setDisable(false);
            btn.getStyleClass().removeAll("correct", "incorrect");
        }
        
        updateScoreDisplay();
    }
    
    private void selectAnswer(int index) {
        if (answered || index < 0 || index >= choiceButtons.size()) {
            return;
        }
        
        answered = true;
        Button selected = choiceButtons.get(index);
        
        // Disable all buttons
        choiceButtons.forEach(btn -> btn.setDisable(true));
        
        if (index == correctIndex) {
            handleCorrectAnswer();
        } else {
            handleIncorrectAnswer();
        }
    }
    
    @Override
    protected void playCorrectAnimation() {
        Button correctBtn = choiceButtons.get(correctIndex);
        correctBtn.getStyleClass().add("correct");
        AnimationFactory.playCorrect(correctBtn);
        AnimationFactory.playCorrect(studentImage);
    }
    
    @Override
    protected void playIncorrectAnimation() {
        // Find which button was selected (the one that's disabled but not correct)
        for (int i = 0; i < choiceButtons.size(); i++) {
            Button btn = choiceButtons.get(i);
            if (i != correctIndex) {
                btn.getStyleClass().add("incorrect");
            }
        }
        AnimationFactory.playIncorrect(studentImage);
    }
    
    @Override
    protected void showCorrectAnswer() {
        Button correctBtn = choiceButtons.get(correctIndex);
        correctBtn.getStyleClass().add("correct");
        
        feedbackLabel.setText("Correct answer: " + session.getCurrentStudent().displayName());
        feedbackLabel.setVisible(true);
        feedbackLabel.setStyle("-fx-text-fill: green;");
    }
    
    @Override
    protected void onStreakBonus() {
        streakLabel.setText("🔥 " + session.getCurrentStreak() + " streak!");
        AnimationFactory.playStreakBonus(streakLabel);
    }
    
    @Override
    protected void showResults() {
        Alert results = new Alert(Alert.AlertType.INFORMATION);
        results.setTitle("Game Complete!");
        results.setHeaderText("Multiple Choice Complete!");
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
        scoreLabel.setText("Score: " + session.getScore());
        progressLabel.setText("Question: " + (session.getQuestionsAnswered() + 1) + " / " + session.getTotalQuestions());
        
        if (session.getCurrentStreak() > 0) {
            streakLabel.setText("🔥 " + session.getCurrentStreak());
            streakLabel.setVisible(true);
        } else {
            streakLabel.setVisible(false);
        }
    }
    
    @FXML
    private void goHome() {
        returnHome();
    }
}
