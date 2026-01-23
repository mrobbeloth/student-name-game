package com.example.namegame.controller;

import com.example.namegame.model.Student;
import com.example.namegame.service.FuzzyMatcher;
import com.example.namegame.service.KeyboardShortcutService;
import com.example.namegame.service.RosterService;
import com.example.namegame.util.AnimationFactory;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.List;

/**
 * Controller for the fill-in-the-blank game mode.
 */
public class FillInBlankController extends GameControllerBase {
    
    @FXML private ImageView studentImage;
    @FXML private TextField answerField;
    @FXML private Button submitButton;
    @FXML private Label scoreLabel;
    @FXML private Label progressLabel;
    @FXML private Label streakLabel;
    @FXML private Label feedbackLabel;
    @FXML private Label hintLabel;
    
    private ContextMenu autocompleteMenu;
    private boolean answered = false;
    
    @FXML
    public void initialize() {
        setupAutocomplete();
        
        // Submit on Enter
        answerField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !answered) {
                submitAnswer();
            }
        });
    }
    
    private void setupAutocomplete() {
        autocompleteMenu = new ContextMenu();
        
        answerField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.length() < 2) {
                autocompleteMenu.hide();
                return;
            }
            
            String search = newVal.toLowerCase();
            List<String> rosterNames = RosterService.getInstance().getRosterNames();
            
            List<MenuItem> suggestions = rosterNames.stream()
                .filter(name -> {
                    String[] parts = RosterService.parseRosterName(name);
                    String firstName = parts[0].toLowerCase();
                    String lastName = parts[1].toLowerCase();
                    String display = (firstName + " " + lastName).toLowerCase();
                    return firstName.startsWith(search) || 
                           lastName.startsWith(search) || 
                           display.startsWith(search);
                })
                .limit(5)
                .map(name -> {
                    String[] parts = RosterService.parseRosterName(name);
                    MenuItem item = new MenuItem(parts[0] + " " + parts[1]);
                    item.setOnAction(e -> {
                        answerField.setText(parts[0] + " " + parts[1]);
                        autocompleteMenu.hide();
                    });
                    return item;
                })
                .toList();
            
            if (suggestions.isEmpty()) {
                autocompleteMenu.hide();
            } else {
                autocompleteMenu.getItems().setAll(suggestions);
                if (!autocompleteMenu.isShowing()) {
                    autocompleteMenu.show(answerField, Side.BOTTOM, 0, 0);
                }
            }
        });
        
        // Hide menu when field loses focus
        answerField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                autocompleteMenu.hide();
            }
        });
    }
    
    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
    }
    
    @Override
    protected void loadQuestion() {
        answered = false;
        feedbackLabel.setText("");
        feedbackLabel.setVisible(false);
        answerField.clear();
        answerField.setDisable(false);
        answerField.setStyle("");
        submitButton.setDisable(false);
        
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
        
        // Show hint (first letter of first name)
        if (hintLabel != null) {
            String hint = current.firstName().substring(0, 1) + "...";
            hintLabel.setText("Hint: " + hint);
        }
        
        answerField.requestFocus();
        updateScoreDisplay();
    }
    
    @FXML
    private void submitAnswer() {
        if (answered) {
            return;
        }
        
        String input = answerField.getText().trim();
        if (input.isEmpty()) {
            answerField.setStyle("-fx-border-color: orange;");
            return;
        }
        
        answered = true;
        answerField.setDisable(true);
        submitButton.setDisable(true);
        
        Student current = session.getCurrentStudent();
        boolean correct = FuzzyMatcher.isNameMatch(input, current.firstName(), current.lastName());
        
        if (correct) {
            handleCorrectAnswer();
        } else {
            handleIncorrectAnswer();
        }
    }
    
    @Override
    protected void playCorrectAnimation() {
        answerField.setStyle("-fx-border-color: green; -fx-border-width: 2;");
        AnimationFactory.playCorrect(answerField);
        AnimationFactory.playCorrect(studentImage);
    }
    
    @Override
    protected void playIncorrectAnimation() {
        answerField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        AnimationFactory.playIncorrect(answerField);
        AnimationFactory.playIncorrect(studentImage);
    }
    
    @Override
    protected void showCorrectAnswer() {
        Student current = session.getCurrentStudent();
        feedbackLabel.setText("Correct answer: " + current.displayName());
        feedbackLabel.setVisible(true);
        feedbackLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
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
        results.setHeaderText("Fill in the Blank Complete!");
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
