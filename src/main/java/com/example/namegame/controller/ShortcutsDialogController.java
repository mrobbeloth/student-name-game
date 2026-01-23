package com.example.namegame.controller;

import com.example.namegame.service.KeyboardShortcutService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;

/**
 * Controller for the keyboard shortcuts dialog.
 */
public class ShortcutsDialogController {
    
    @FXML private TableView<Map.Entry<String, String>> shortcutsTable;
    @FXML private TableColumn<Map.Entry<String, String>, String> actionColumn;
    @FXML private TableColumn<Map.Entry<String, String>, String> shortcutColumn;
    
    @FXML
    public void initialize() {
        actionColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getKey()));
        
        shortcutColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getValue()));
        
        Map<String, String> shortcuts = KeyboardShortcutService.getShortcutDescriptions();
        shortcutsTable.setItems(FXCollections.observableArrayList(shortcuts.entrySet()));
    }
}
