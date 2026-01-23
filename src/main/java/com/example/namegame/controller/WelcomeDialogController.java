package com.example.namegame.controller;

import com.example.namegame.service.ConfigService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

/**
 * Controller for the welcome dialog shown on first launch.
 */
public class WelcomeDialogController {
    
    @FXML private Label pathLabel;
    @FXML private Button selectButton;
    @FXML private Button startButton;
    
    private Stage stage;
    private Path selectedPath;
    private Runnable onComplete;
    
    @FXML
    public void initialize() {
        startButton.setDisable(true);
        pathLabel.setText("No folder selected");
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setOnComplete(Runnable callback) {
        this.onComplete = callback;
    }
    
    @FXML
    private void selectFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Images and Roster Folder");
        
        // Start from user home or last known location
        Path current = ConfigService.getInstance().getImagesDirectory();
        if (current != null && current.toFile().exists()) {
            chooser.setInitialDirectory(current.toFile());
        } else {
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        
        File selected = chooser.showDialog(stage);
        if (selected != null) {
            selectedPath = selected.toPath();
            pathLabel.setText(selectedPath.toString());
            
            // Validate folder has roster file
            boolean hasRoster = selectedPath.resolve("roster.xls").toFile().exists() ||
                               selectedPath.resolve("roster.xlsx").toFile().exists();
            
            if (hasRoster) {
                startButton.setDisable(false);
                pathLabel.setStyle("-fx-text-fill: green;");
            } else {
                startButton.setDisable(true);
                pathLabel.setStyle("-fx-text-fill: orange;");
                
                Alert warning = new Alert(Alert.AlertType.WARNING);
                warning.setTitle("Missing Roster");
                warning.setHeaderText("No roster file found");
                warning.setContentText("The selected folder should contain a roster.xls or roster.xlsx file with a 'Name' column.");
                warning.showAndWait();
            }
        }
    }
    
    @FXML
    private void getStarted() {
        if (selectedPath == null) {
            return;
        }
        
        ConfigService config = ConfigService.getInstance();
        config.setImagesDirectory(selectedPath);
        config.markFirstLaunchComplete();
        
        stage.close();
        
        if (onComplete != null) {
            onComplete.run();
        }
    }
}
