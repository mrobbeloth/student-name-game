package com.example.namegame.controller;

import com.example.namegame.model.Student;
import com.example.namegame.util.AnimationFactory;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

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
    
    // Scaling variables
    private static final double BASE_WIDTH = 1200.0; // Reference window width
    private static final double BASE_HEIGHT = 800.0; // Reference window height
    private static final double BASE_IMAGE_SIZE = 100.0;
    private static final double BASE_BUTTON_WIDTH = 120.0;
    private static final double BASE_BUTTON_HEIGHT = 140.0;
    private static final double BASE_NAME_WIDTH = 150.0;
    private static final double BASE_NAME_HEIGHT = 40.0;
    private double currentScaleFactor = 1.0;
    
    // Hover preview variables
    private static final double PREVIEW_IMAGE_SIZE = 200.0; // Base size for preview
    private Pane hoverPreviewPane;
    private ImageView hoverPreviewImage;
    private FadeTransition fadeInTransition;
    private FadeTransition fadeOutTransition;
    
    @FXML
    public void initialize() {
        // Initialize hover preview system
        setupHoverPreview();
        
        // Initialize scaling when the scene is available
        if (imagePane != null) {
            imagePane.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    setupWindowResizeListener();
                    // Add the hover preview to the scene root after scene is available
                    addHoverPreviewToScene();
                }
            });
        }
    }
    
    @Override
    protected void initializeGame() {
        super.initializeGame();
        setupMatchingGrid();
    }
    
    /**
     * Sets up window resize listener for responsive scaling
     */
    private void setupWindowResizeListener() {
        if (imagePane.getScene() != null && imagePane.getScene().getWindow() != null) {
            ChangeListener<Number> resizeListener = new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    updateScaling();
                }
            };
            
            imagePane.getScene().getWindow().widthProperty().addListener(resizeListener);
            imagePane.getScene().getWindow().heightProperty().addListener(resizeListener);
            
            // Initial scaling
            updateScaling();
        }
    }
    
    /**
     * Updates scaling based on current window size
     */
    private void updateScaling() {
        if (imagePane.getScene() == null || imagePane.getScene().getWindow() == null) {
            return;
        }
        
        double windowWidth = imagePane.getScene().getWindow().getWidth();
        double windowHeight = imagePane.getScene().getWindow().getHeight();
        
        // Calculate scale factors based on both width and height, use the smaller one
        double widthScale = windowWidth / BASE_WIDTH;
        double heightScale = windowHeight / BASE_HEIGHT;
        currentScaleFactor = Math.min(widthScale, heightScale);
        
        // Ensure minimum scale factor
        currentScaleFactor = Math.max(currentScaleFactor, 0.5);
        
        // Apply scaling to all elements
        scaleAllElements();
    }
    
    /**
     * Sets up the hover preview system
     */
    private void setupHoverPreview() {
        // Create preview pane
        hoverPreviewPane = new Pane();
        hoverPreviewPane.setVisible(false);
        hoverPreviewPane.setMouseTransparent(true);
        hoverPreviewPane.getStyleClass().add("hover-preview-pane");
        
        // Create preview image
        hoverPreviewImage = new ImageView();
        hoverPreviewImage.setPreserveRatio(true);
        hoverPreviewImage.setSmooth(true);
        hoverPreviewImage.getStyleClass().add("hover-preview-image");
        
        // Style the preview pane
        hoverPreviewPane.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #3498db;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        
        hoverPreviewPane.getChildren().add(hoverPreviewImage);
        
        // Setup animations
        fadeInTransition = new FadeTransition(Duration.millis(200), hoverPreviewPane);
        fadeInTransition.setFromValue(0.0);
        fadeInTransition.setToValue(1.0);
        
        fadeOutTransition = new FadeTransition(Duration.millis(150), hoverPreviewPane);
        fadeOutTransition.setFromValue(1.0);
        fadeOutTransition.setToValue(0.0);
        fadeOutTransition.setOnFinished(e -> hoverPreviewPane.setVisible(false));
    }
    
    /**
     * Adds the hover preview to the scene root
     */
    private void addHoverPreviewToScene() {
        if (imagePane.getScene() != null && imagePane.getScene().getRoot() instanceof Pane rootPane) {
            if (!rootPane.getChildren().contains(hoverPreviewPane)) {
                rootPane.getChildren().add(hoverPreviewPane);
            }
        }
    }
    
    /**
     * Applies scaling to all UI elements
     */
    private void scaleAllElements() {
        // Scale image buttons
        for (Button button : imageButtons.keySet()) {
            scaleImageButton(button);
        }
        
        // Scale name buttons
        for (Button button : nameButtons.keySet()) {
            scaleNameButton(button);
        }
        
        // Scale scroll panes
        scaleScrollPanes();
        
        // Scale labels and text
        scaleLabelsAndText();
    }
    
    /**
     * Scales an image button and its contents
     */
    private void scaleImageButton(Button button) {
        double scaledWidth = BASE_BUTTON_WIDTH * currentScaleFactor;
        double scaledHeight = BASE_BUTTON_HEIGHT * currentScaleFactor;
        double scaledImageSize = BASE_IMAGE_SIZE * currentScaleFactor;
        
        button.setPrefSize(scaledWidth, scaledHeight);
        button.setMinSize(scaledWidth, scaledHeight);
        
        // Scale the image inside
        if (button.getGraphic() instanceof ImageView imageView) {
            imageView.setFitWidth(scaledImageSize);
            imageView.setFitHeight(scaledImageSize);
        }
    }
    
    /**
     * Scales a name button
     */
    private void scaleNameButton(Button button) {
        double scaledWidth = BASE_NAME_WIDTH * currentScaleFactor;
        double scaledHeight = BASE_NAME_HEIGHT * currentScaleFactor;
        
        button.setPrefSize(scaledWidth, scaledHeight);
        button.setMinWidth(scaledWidth);
        
        // Scale font size
        Font currentFont = button.getFont();
        double baseFontSize = 12.0;
        double scaledFontSize = baseFontSize * currentScaleFactor;
        scaledFontSize = Math.max(scaledFontSize, 8.0); // Minimum font size
        button.setFont(Font.font(currentFont.getFamily(), scaledFontSize));
    }
    
    /**
     * Scales scroll panes
     */
    private void scaleScrollPanes() {
        double baseScrollHeight = 400.0;
        double scaledScrollHeight = baseScrollHeight * currentScaleFactor;
        
        // Find scroll panes in the UI tree
        scaleScrollPanesRecursive(imagePane.getParent());
    }
    
    /**
     * Recursively finds and scales scroll panes
     */
    private void scaleScrollPanesRecursive(Node node) {
        if (node instanceof ScrollPane scrollPane) {
            double baseScrollHeight = 400.0;
            double scaledScrollHeight = baseScrollHeight * currentScaleFactor;
            scrollPane.setPrefHeight(scaledScrollHeight);
        } else if (node instanceof Pane pane) {
            for (Node child : pane.getChildren()) {
                scaleScrollPanesRecursive(child);
            }
        }
    }
    
    /**
     * Scales labels and text elements
     */
    private void scaleLabelsAndText() {
        // Scale flow pane gaps
        double baseGap = 10.0;
        double scaledGap = baseGap * currentScaleFactor;
        imagePane.setHgap(scaledGap);
        imagePane.setVgap(scaledGap);
        namePane.setHgap(scaledGap);
        namePane.setVgap(scaledGap);
        
        // Scale wrap lengths
        double baseImageWrap = 400.0;
        double baseNameWrap = 300.0;
        imagePane.setPrefWrapLength(baseImageWrap * currentScaleFactor);
        namePane.setPrefWrapLength(baseNameWrap * currentScaleFactor);
        
        // Scale labels in header
        scaleLabelsRecursive(scoreLabel);
        scaleLabelsRecursive(progressLabel);
        scaleLabelsRecursive(streakLabel);
    }
    
    /**
     * Scales individual labels
     */
    private void scaleLabelsRecursive(Node node) {
        if (node instanceof Label label) {
            Font currentFont = label.getFont();
            double baseFontSize = 14.0;
            if (label.getStyleClass().contains("game-title")) {
                baseFontSize = 24.0;
            } else if (label.getStyleClass().contains("section-label")) {
                baseFontSize = 16.0;
            } else if (label.getStyleClass().contains("instruction-label")) {
                baseFontSize = 14.0;
            }
            
            double scaledFontSize = baseFontSize * currentScaleFactor;
            scaledFontSize = Math.max(scaledFontSize, 8.0); // Minimum font size
            label.setFont(Font.font(currentFont.getFamily(), scaledFontSize));
        }
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
        
        // Apply initial scaling if window is available
        javafx.application.Platform.runLater(() -> {
            if (imagePane.getScene() != null && imagePane.getScene().getWindow() != null) {
                updateScaling();
            }
        });
    }
    
    private Button createImageButton(Student student) {
        Button btn = new Button();
        btn.setPrefSize(BASE_BUTTON_WIDTH, BASE_BUTTON_HEIGHT);
        btn.setMinSize(BASE_BUTTON_WIDTH, BASE_BUTTON_HEIGHT);
        btn.getStyleClass().add("image-card");
        
        try {
            Image image = new Image(new FileInputStream(student.imagePath().toFile()), BASE_IMAGE_SIZE, BASE_IMAGE_SIZE, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(BASE_IMAGE_SIZE);
            imageView.setFitHeight(BASE_IMAGE_SIZE);
            imageView.setPreserveRatio(true);
            btn.setGraphic(imageView);
            
            // Add hover preview functionality
            setupImageHoverPreview(btn, student);
            
        } catch (Exception e) {
            btn.setText("?");
        }
        
        btn.setOnAction(e -> selectImage(btn));
        return btn;
    }
    
    private Button createNameButton(Student student) {
        Button btn = new Button(student.displayName());
        btn.setPrefSize(BASE_NAME_WIDTH, BASE_NAME_HEIGHT);
        btn.setMinWidth(BASE_NAME_WIDTH);
        btn.getStyleClass().add("name-card");
        btn.setWrapText(true);
        
        btn.setOnAction(e -> selectName(btn));
        return btn;
    }
    
    /**
     * Sets up hover preview for an image button
     */
    private void setupImageHoverPreview(Button button, Student student) {
        button.setOnMouseEntered(e -> showHoverPreview(e, student));
        button.setOnMouseExited(e -> hideHoverPreview());
        button.setOnMouseMoved(e -> updateHoverPreviewPosition(e));
    }
    
    /**
     * Shows the hover preview with the student's image
     */
    private void showHoverPreview(MouseEvent event, Student student) {
        if (hoverPreviewPane == null || hoverPreviewImage == null) {
            return;
        }
        
        try {
            // Load a larger version of the image for preview
            double previewSize = PREVIEW_IMAGE_SIZE * currentScaleFactor;
            Image previewImage = new Image(new FileInputStream(student.imagePath().toFile()), 
                                         previewSize, previewSize, true, true);
            hoverPreviewImage.setImage(previewImage);
            hoverPreviewImage.setFitWidth(previewSize);
            hoverPreviewImage.setFitHeight(previewSize);
            
            // Size the preview pane to fit the image with padding
            double padding = 10 * currentScaleFactor;
            hoverPreviewPane.setPrefWidth(previewSize + padding * 2);
            hoverPreviewPane.setPrefHeight(previewSize + padding * 2);
            hoverPreviewImage.setLayoutX(padding);
            hoverPreviewImage.setLayoutY(padding);
            
            // Position and show the preview
            updateHoverPreviewPosition(event);
            
            // Stop any ongoing fade out and start fade in
            fadeOutTransition.stop();
            hoverPreviewPane.setVisible(true);
            fadeInTransition.play();
            
        } catch (Exception ex) {
            // If image loading fails, don't show preview
            hideHoverPreview();
        }
    }
    
    /**
     * Updates the position of the hover preview based on mouse position
     */
    private void updateHoverPreviewPosition(MouseEvent event) {
        if (hoverPreviewPane == null || !hoverPreviewPane.isVisible() || imagePane.getScene() == null) {
            return;
        }
        
        // Get mouse position in scene coordinates
        double mouseX = event.getSceneX();
        double mouseY = event.getSceneY();
        
        // Get scene dimensions
        double sceneWidth = imagePane.getScene().getWidth();
        double sceneHeight = imagePane.getScene().getHeight();
        
        // Calculate preview position with offset from cursor
        double offsetX = 20 * currentScaleFactor;
        double offsetY = 10 * currentScaleFactor;
        
        double previewX = mouseX + offsetX;
        double previewY = mouseY + offsetY;
        
        // Check if preview would go off-screen and adjust position
        double previewWidth = hoverPreviewPane.getPrefWidth();
        double previewHeight = hoverPreviewPane.getPrefHeight();
        
        // Adjust X position if it would go off the right edge
        if (previewX + previewWidth > sceneWidth) {
            previewX = mouseX - previewWidth - offsetX;
        }
        
        // Adjust Y position if it would go off the bottom edge
        if (previewY + previewHeight > sceneHeight) {
            previewY = mouseY - previewHeight - offsetY;
        }
        
        // Ensure preview doesn't go off the left or top edges
        previewX = Math.max(10, previewX);
        previewY = Math.max(10, previewY);
        
        hoverPreviewPane.setLayoutX(previewX);
        hoverPreviewPane.setLayoutY(previewY);
    }
    
    /**
     * Hides the hover preview
     */
    private void hideHoverPreview() {
        if (hoverPreviewPane != null && hoverPreviewPane.isVisible()) {
            fadeInTransition.stop();
            fadeOutTransition.play();
        }
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
