package com.example.namegame;

import com.example.namegame.controller.MainDashboardController;
import com.example.namegame.controller.WelcomeDialogController;
import com.example.namegame.service.ConfigService;
import com.example.namegame.service.ImageService;
import com.example.namegame.service.KeyboardShortcutService;
import com.example.namegame.service.SoundService;
import com.example.namegame.util.AppLogger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main application entry point for Student Name Game.
 */
public class NameGameApplication extends Application {
    
    private static final String APP_TITLE = "Student Name Game";
    private static final String APP_VERSION = "1.0.3";
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 700;

    // -----------------------------------------------------------------------
    // Logging helpers
    // -----------------------------------------------------------------------

    /** Resolves a writable log-file path.  Falls back to the system temp dir. */
    private static Path resolveLogFile() {
        // Preferred: ~/.namegame/namegame.log  (same dir ConfigService uses)
        try {
            Path dir = Path.of(System.getProperty("user.home"), ".namegame");
            Files.createDirectories(dir);
            return dir.resolve("namegame.log");
        } catch (Exception ignored) { /* fall through */ }

        // Fallback: %TEMP%\namegame.log
        try {
            return Path.of(System.getProperty("java.io.tmpdir"), "namegame.log");
        } catch (Exception ignored) { /* fall through */ }

        return null; // logging completely disabled
    }

    // -----------------------------------------------------------------------
    // Fatal-error dialog (replaces a missing stderr console in GUI installs)
    // -----------------------------------------------------------------------

    private void showFatalErrorDialog(Stage owner, Throwable t) {
        try {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(APP_TITLE + " – Fatal Error");
            alert.setHeaderText("The application failed to start.");
            Path logFile = AppLogger.getLogFile();
            String logHint = (logFile != null)
                    ? "\n\nFull details: " + logFile
                    : "";
            alert.setContentText("Error: " + t.getMessage() + logHint);

            TextArea textArea = new TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(textArea, Priority.ALWAYS);

            VBox expContent = new VBox(textArea);
            expContent.setMaxWidth(Double.MAX_VALUE);
            alert.getDialogPane().setExpandableContent(expContent);
            alert.getDialogPane().setExpanded(true);

            alert.showAndWait();
        } catch (Exception dialogEx) {
            AppLogger.log("Failed to show fatal-error dialog", dialogEx);
        } finally {
            Platform.exit();
        }
    }

    // -----------------------------------------------------------------------
    // Application lifecycle
    // -----------------------------------------------------------------------

    @Override
    public void start(Stage primaryStage) {
        try {
            AppLogger.log("start() called");
            ConfigService config = ConfigService.getInstance();

            // Initialize SoundService early to load sounds
            SoundService.getInstance();

            if (config.isFirstLaunch()) {
                showWelcomeDialog(primaryStage);
            } else {
                loadImagesAndShowDashboard(primaryStage);
            }
        } catch (Exception e) {
            AppLogger.log("Fatal error during startup", e);
            showFatalErrorDialog(primaryStage, e);
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
                AppLogger.log("Error loading dashboard after welcome dialog", e);
                Platform.runLater(() -> showFatalErrorDialog(primaryStage, e));
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
        // Initialise the log file before anything else so every startup attempt
        // is recorded — even if it happens before the JavaFX toolkit starts.
        AppLogger.init(resolveLogFile());
        AppLogger.log("=== Student Name Game starting (version " + APP_VERSION + ") ===");
        AppLogger.log("java.version=" + System.getProperty("java.version")
                + "  os.name=" + System.getProperty("os.name")
                + "  user.home=" + System.getProperty("user.home"));

        // Catch any thread that dies without an explicit handler (non-FX threads).
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                AppLogger.log("Uncaught exception on thread \"" + thread.getName() + "\"", throwable));

        try {
            launch(args);
        } catch (Exception e) {
            AppLogger.log("Fatal error in launch()", e);
        }
    }
}
