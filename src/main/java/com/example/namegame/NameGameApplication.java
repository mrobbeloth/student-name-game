package com.example.namegame;

import com.example.namegame.controller.MainDashboardController;
import com.example.namegame.controller.WelcomeDialogController;
import com.example.namegame.service.ConfigService;
import com.example.namegame.service.ImageService;
import com.example.namegame.service.KeyboardShortcutService;
import com.example.namegame.service.SoundService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point for Student Name Game.
 */
public class NameGameApplication extends Application {
    
    private static final String APP_TITLE = "Student Name Game";
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 700;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        ConfigService config = ConfigService.getInstance();
        
        // Initialize SoundService early to load sounds
        SoundService.getInstance();
        
        if (config.isFirstLaunch()) {
            showWelcomeDialog(primaryStage);
        } else {
            loadImagesAndShowDashboard(primaryStage);
        }
    }
    
    private void showWelcomeDialog(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/namegame/views/welcome-dialog.fxml"));
        Parent root = loader.load();
        
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Welcome - " + APP_TITLE);
        dialogStage.setResizable(false);
        
        WelcomeDialogController controller = loader.getController();
        controller.setStage(dialogStage);
        controller.setOnComplete(() -> {
            try {
                loadImagesAndShowDashboard(primaryStage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/namegame/css/style.css").toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
    
    private void loadImagesAndShowDashboard(Stage primaryStage) throws Exception {
        // Load images
        ImageService.getInstance().loadImages();
        
        // Show dashboard
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/namegame/views/main-view.fxml"));
        Parent root = loader.load();
        
        MainDashboardController controller = loader.getController();
        
        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/com/example/namegame/css/style.css").toExternalForm());

        // Register keyboard shortcuts
        KeyboardShortcutService shortcutService = KeyboardShortcutService.getInstance();
        shortcutService.registerScene(scene);
        shortcutService.addGlobalShortcut(KeyboardShortcutService.QUIT, Platform::exit);

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        controller.setStage(primaryStage);
        
        primaryStage.show();
        
        // Check for unmatched images
        if (!ImageService.getInstance().getUnmatchedImages().isEmpty()) {
            controller.viewUnmatched();
        }
    }
    
    @Override
    public void stop() {
        // Clean up resources
        ImageService.getInstance().stopWatching();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
