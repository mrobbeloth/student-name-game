package com.example.namegame.service;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.HashMap;
import java.util.Map;


/**
 * Manages keyboard shortcuts across the application.
 */
public class KeyboardShortcutService {
    private static KeyboardShortcutService instance;
    
    private final Map<KeyCombination, Runnable> globalShortcuts;
    private Scene currentScene;
    
    // Standard shortcuts
    public static final KeyCombination SETTINGS = new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN);    public static final KeyCombination QUIT = new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN);    public static final KeyCode RELOAD = KeyCode.R;
    public static final KeyCode SUBMIT = KeyCode.ENTER;
    public static final KeyCode CHOICE_1 = KeyCode.DIGIT1;
    public static final KeyCode CHOICE_2 = KeyCode.DIGIT2;
    public static final KeyCode CHOICE_3 = KeyCode.DIGIT3;
    public static final KeyCode CHOICE_4 = KeyCode.DIGIT4;
    
    private KeyboardShortcutService() {
        this.globalShortcuts = new HashMap<>();
    }
    
    public static synchronized KeyboardShortcutService getInstance() {
        if (instance == null) {
            instance = new KeyboardShortcutService();
        }
        return instance;
    }
    
    /**
     * Registers global shortcuts on a scene.
     */
    public void registerScene(Scene scene) {
        this.currentScene = scene;
        
        scene.setOnKeyPressed(event -> {
            // Check key combinations first
            for (Map.Entry<KeyCombination, Runnable> entry : globalShortcuts.entrySet()) {
                if (entry.getKey().match(event)) {
                    entry.getValue().run();
                    event.consume();
                    return;
                }
            }
        });
    }
    
    /**
     * Adds a global shortcut.
     */
    public void addGlobalShortcut(KeyCombination combination, Runnable action) {
        globalShortcuts.put(combination, action);
    }
    
    /**
     * Removes a global shortcut.
     */
    public void removeGlobalShortcut(KeyCombination combination) {
        globalShortcuts.remove(combination);
    }
    
    /**
     * Gets the display string for a key combination.
     */
    public static String getDisplayString(KeyCombination combination) {
        return combination.getDisplayText();
    }
    
    /**
     * Gets the display string for a key code.
     */
    public static String getDisplayString(KeyCode keyCode) {
        return keyCode.getName();
    }
    
    /**
     * Returns true if the current OS is macOS.
     */
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
    
    /**
     * Gets the modifier key name for the current OS.
     */
    public static String getModifierKeyName() {
        return isMac() ? "Cmd" : "Ctrl";
    }
    
    /**
     * Gets all documented shortcuts as a map of description to shortcut string.
     */
    public static Map<String, String> getShortcutDescriptions() {
        Map<String, String> shortcuts = new java.util.LinkedHashMap<>();
        String mod = getModifierKeyName();
        
        shortcuts.put("Open Settings", mod + " + ,");
        shortcuts.put("Exit Application", mod + " + Q");
        shortcuts.put("Reload Images", "R");
        shortcuts.put("Submit Answer (Fill-in-Blank)", "Enter");
        shortcuts.put("Multiple Choice Answer 1", "1");
        shortcuts.put("Multiple Choice Answer 2", "2");
        shortcuts.put("Multiple Choice Answer 3", "3");
        shortcuts.put("Multiple Choice Answer 4", "4");
        
        return shortcuts;
    }
}
