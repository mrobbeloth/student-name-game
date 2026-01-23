package com.example.namegame.controller;

import com.example.namegame.model.GameMode;
import com.example.namegame.model.Student;
import com.example.namegame.model.UnmatchedImage;
import com.example.namegame.service.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the main dashboard view.
 */
public class MainDashboardController {
    
    @FXML private VBox matchingCard;
    @FXML private VBox multipleChoiceCard;
    @FXML private VBox fillInBlankCard;
    
    @FXML private Label studentCountLabel;
    @FXML private Label unmatchedWarningLabel;
    
    @FXML private Label totalMatchesLabel;
    @FXML private Label totalMissesLabel;
    @FXML private Label accuracyLabel;
    @FXML private Label gamesPlayedLabel;
    @FXML private Label bestStreakLabel;
    
    @FXML private MenuBar menuBar;
    
    private Stage stage;
    
    @FXML
    public void initialize() {
        updateStatistics();
        updateStudentInfo();
        setupGameCards();
        
        // Register keyboard shortcut for settings
        KeyboardShortcutService.getInstance().addGlobalShortcut(
            KeyboardShortcutService.SETTINGS,
            this::openSettings
        );
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
        KeyboardShortcutService.getInstance().registerScene(stage.getScene());
        
        // Setup file watcher
        ImageService.getInstance().startWatching(v -> {
            showReloadNotification();
        });
    }
    
    private void setupGameCards() {
        List<Student> students = ImageService.getInstance().getStudents();
        int count = students.size();
        
        // Matching - needs at least 1 student
        matchingCard.setDisable(count < 1);
        if (count < 1) {
            matchingCard.setOpacity(0.5);
            Tooltip.install(matchingCard, new Tooltip("Requires at least 1 student"));
        }
        
        // Multiple Choice - needs at least 4 students
        multipleChoiceCard.setDisable(count < 4);
        if (count < 4) {
            multipleChoiceCard.setOpacity(0.5);
            Tooltip.install(multipleChoiceCard, new Tooltip("Requires at least 4 students"));
        }
        
        // Fill in the Blank - needs at least 1 student
        fillInBlankCard.setDisable(count < 1);
        if (count < 1) {
            fillInBlankCard.setOpacity(0.5);
            Tooltip.install(fillInBlankCard, new Tooltip("Requires at least 1 student"));
        }
    }
    
    private void updateStatistics() {
        StatisticsService stats = StatisticsService.getInstance();
        totalMatchesLabel.setText(String.valueOf(stats.getTotalMatches()));
        totalMissesLabel.setText(String.valueOf(stats.getTotalMisses()));
        accuracyLabel.setText(String.format("%.1f%%", stats.getLifetimeAccuracy()));
        gamesPlayedLabel.setText(String.valueOf(stats.getGamesPlayed()));
        bestStreakLabel.setText(String.valueOf(stats.getBestStreak()));
    }
    
    private void updateStudentInfo() {
        List<Student> students = ImageService.getInstance().getStudents();
        List<UnmatchedImage> unmatched = ImageService.getInstance().getUnmatchedImages();
        
        studentCountLabel.setText(students.size() + " students loaded");
        
        if (!unmatched.isEmpty()) {
            unmatchedWarningLabel.setText("⚠ " + unmatched.size() + " unmatched images");
            unmatchedWarningLabel.setVisible(true);
            unmatchedWarningLabel.setStyle("-fx-text-fill: orange;");
        } else {
            unmatchedWarningLabel.setVisible(false);
        }
    }
    
    @FXML
    private void startMatching() {
        startGame(GameMode.MATCHING);
    }
    
    @FXML
    private void startMultipleChoice() {
        startGame(GameMode.MULTIPLE_CHOICE);
    }
    
    @FXML
    private void startFillInBlank() {
        startGame(GameMode.FILL_IN_BLANK);
    }
    
    private void startGame(GameMode mode) {
        try {
            String fxmlFile = switch (mode) {
                case MATCHING -> "/com/example/namegame/views/matching-view.fxml";
                case MULTIPLE_CHOICE -> "/com/example/namegame/views/multiplechoice-view.fxml";
                case FILL_IN_BLANK -> "/com/example/namegame/views/fillinblank-view.fxml";
            };
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof GameControllerBase gameController) {
                gameController.setStage(stage);
                gameController.setMode(mode);
                gameController.setOnGameComplete(() -> {
                    returnToDashboard();
                    updateStatistics();
                });
            }
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/namegame/css/style.css").toExternalForm());
            stage.setScene(scene);
            
        } catch (IOException e) {
            showError("Failed to start game", e.getMessage());
        }
    }
    
    private void returnToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/namegame/views/main-view.fxml"));
            Parent root = loader.load();
            
            MainDashboardController controller = loader.getController();
            controller.setStage(stage);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/namegame/css/style.css").toExternalForm());
            stage.setScene(scene);
            
        } catch (IOException e) {
            showError("Failed to return to dashboard", e.getMessage());
        }
    }
    
    @FXML
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/namegame/views/settings-dialog.fxml"));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Settings");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            
            SettingsDialogController controller = loader.getController();
            controller.setStage(dialogStage);
            controller.setOnSave(() -> {
                reloadImages();
            });
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/namegame/css/style.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            showError("Failed to open settings", e.getMessage());
        }
    }
    
    @FXML
    private void exportData() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Data");
        chooser.setInitialFileName(ExportService.getInstance().generateBackupFilename());
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files", "*.zip"));
        
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            try {
                ExportService.getInstance().exportData(file.toPath());
                showInfo("Export Complete", "Data exported to " + file.getName());
            } catch (IOException e) {
                showError("Export Failed", e.getMessage());
            }
        }
    }
    
    @FXML
    private void importData() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Data");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files", "*.zip"));
        
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Import");
            confirm.setHeaderText("Import will overwrite existing data");
            confirm.setContentText("Are you sure you want to import from " + file.getName() + "?");
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    ExportService.getInstance().importData(file.toPath());
                    updateStatistics();
                    reloadImages();
                    showInfo("Import Complete", "Data imported successfully");
                } catch (IOException e) {
                    showError("Import Failed", e.getMessage());
                }
            }
        }
    }
    
    @FXML
    private void reloadImages() {
        ImageService.getInstance().reload();
        updateStudentInfo();
        setupGameCards();
    }
    
    @FXML
    public void viewUnmatched() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/namegame/views/unmatched-dialog.fxml"));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Unmatched Images");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            
            UnmatchedDialogController controller = loader.getController();
            controller.setStage(dialogStage);
            controller.setOnComplete(() -> {
                reloadImages();
            });
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/namegame/css/style.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            showError("Failed to open unmatched dialog", e.getMessage());
        }
    }
    
    @FXML
    private void resetStatistics() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Statistics");
        confirm.setHeaderText("This will delete all your game statistics");
        confirm.setContentText("Are you sure you want to reset?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            StatisticsService.getInstance().reset();
            updateStatistics();
            showInfo("Statistics Reset", "All statistics have been cleared");
        }
    }
    
    @FXML
    private void showKeyboardShortcuts() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/namegame/views/shortcuts-dialog.fxml"));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Keyboard Shortcuts");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/namegame/css/style.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            showError("Failed to open shortcuts", e.getMessage());
        }
    }
    
    @FXML
    private void showAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About Student Name Game");
        about.setHeaderText("Student Name Game v1.0.0");
        about.setContentText("""
            A gamified application for learning student names.
            
            Features:
            • Matching game
            • Multiple choice quiz
            • Fill-in-the-blank
            
            Built with Java 21 and JavaFX 21.
            """);
        about.showAndWait();
    }
    
    @FXML
    private void exitApplication() {
        ImageService.getInstance().stopWatching();
        stage.close();
    }
    
    private void showReloadNotification() {
        unmatchedWarningLabel.setText("📁 Files changed - Press R to reload");
        unmatchedWarningLabel.setStyle("-fx-text-fill: blue;");
        unmatchedWarningLabel.setVisible(true);
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
