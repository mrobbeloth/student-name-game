package com.example.namegame.controller;

import com.example.namegame.service.ConfigService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

/**
 * Controller for the settings dialog.
 */
public class SettingsDialogController {
    
    @FXML private TextField pathField;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private Stage stage;
    private Path selectedPath;
    private Runnable onSave;
    
    @FXML
    public void initialize() {
        Path current = ConfigService.getInstance().getImagesDirectory();
        if (current != null) {
            pathField.setText(current.toString());
            selectedPath = current;
        }
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setOnSave(Runnable callback) {
        this.onSave = callback;
    }
    
    @FXML
    private void browse() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Images and Roster Folder");
        
        if (selectedPath != null && selectedPath.toFile().exists()) {
            chooser.setInitialDirectory(selectedPath.toFile());
        } else {
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        
        File selected = chooser.showDialog(stage);
        if (selected != null) {
            selectedPath = selected.toPath();
            pathField.setText(selectedPath.toString());
        }
    }
    
    @FXML
    private void save() {
        String path = pathField.getText().trim();
        if (path.isEmpty()) {
            showError("Path is required");
            return;
        }
        
        Path newPath = Path.of(path);
        if (!newPath.toFile().isDirectory()) {
            showError("Invalid directory path");
            return;
        }
        
        // Check for roster file
        boolean hasRoster = newPath.resolve("roster.xls").toFile().exists() ||
                           newPath.resolve("roster.xlsx").toFile().exists();
        
        if (!hasRoster) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("No Roster Found");
            confirm.setHeaderText("No roster file detected");
            confirm.setContentText("The folder should contain roster.xls or roster.xlsx. Continue anyway?");
            
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }
        
        ConfigService.getInstance().setImagesDirectory(newPath);
        stage.close();
        
        if (onSave != null) {
            onSave.run();
        }
    }
    
    @FXML
    private void cancel() {
        stage.close();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
