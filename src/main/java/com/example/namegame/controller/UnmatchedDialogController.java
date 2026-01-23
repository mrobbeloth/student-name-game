package com.example.namegame.controller;

import com.example.namegame.model.ScoredMatch;
import com.example.namegame.model.UnmatchedImage;
import com.example.namegame.service.ImageService;
import com.example.namegame.service.RosterService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.List;

/**
 * Controller for the unmatched images dialog.
 */
public class UnmatchedDialogController {
    
    @FXML private TableView<UnmatchedImage> unmatchedTable;
    @FXML private TableColumn<UnmatchedImage, ImageView> thumbnailColumn;
    @FXML private TableColumn<UnmatchedImage, String> filenameColumn;
    @FXML private TableColumn<UnmatchedImage, String> assignColumn;
    @FXML private Button assignAllButton;
    @FXML private Button closeButton;
    @FXML private Label statusLabel;
    
    private Stage stage;
    private Runnable onComplete;
    private ObservableList<UnmatchedImage> unmatchedList;
    
    @FXML
    public void initialize() {
        setupTable();
        loadUnmatchedImages();
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setOnComplete(Runnable callback) {
        this.onComplete = callback;
    }
    
    private void setupTable() {
        // Thumbnail column
        thumbnailColumn.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(64);
                imageView.setFitHeight(64);
                imageView.setPreserveRatio(true);
            }
            
            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    UnmatchedImage unmatched = getTableRow().getItem();
                    try {
                        Image img = new Image(new FileInputStream(unmatched.path().toFile()), 64, 64, true, true);
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });
        
        // Filename column
        filenameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().filename()));
        
        // Assignment column with ComboBox
        List<String> rosterNames = RosterService.getInstance().getRosterNames();
        ObservableList<String> rosterOptions = FXCollections.observableArrayList(rosterNames);
        
        assignColumn.setCellFactory(col -> {
            ComboBoxTableCell<UnmatchedImage, String> cell = new ComboBoxTableCell<>(rosterOptions) {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty && getTableRow() != null && getTableRow().getItem() != null) {
                        UnmatchedImage unmatched = getTableRow().getItem();
                        ScoredMatch best = unmatched.bestSuggestion();
                        if (best != null && item == null) {
                            // Pre-select best suggestion
                            setText(best.rosterName() + (best.isStrongMatch() ? " ✓" : " ?"));
                        }
                    }
                }
            };
            cell.setComboBoxEditable(false);
            return cell;
        });
        
        assignColumn.setCellValueFactory(data -> {
            ScoredMatch best = data.getValue().bestSuggestion();
            return new SimpleStringProperty(best != null ? best.rosterName() : "");
        });
        
        assignColumn.setOnEditCommit(event -> {
            UnmatchedImage image = event.getRowValue();
            String selectedName = event.getNewValue();
            if (selectedName != null && !selectedName.isEmpty()) {
                assignImage(image, selectedName);
            }
        });
        
        unmatchedTable.setEditable(true);
    }
    
    private void loadUnmatchedImages() {
        unmatchedList = FXCollections.observableArrayList(
            ImageService.getInstance().getUnmatchedImages()
        );
        unmatchedTable.setItems(unmatchedList);
        updateStatus();
    }
    
    private void assignImage(UnmatchedImage image, String rosterName) {
        ImageService.getInstance().assignUnmatched(image, rosterName);
        unmatchedList.remove(image);
        updateStatus();
    }
    
    @FXML
    private void assignAllSuggestions() {
        List<UnmatchedImage> toAssign = List.copyOf(unmatchedList);
        for (UnmatchedImage image : toAssign) {
            ScoredMatch best = image.bestSuggestion();
            if (best != null && best.isStrongMatch()) {
                assignImage(image, best.rosterName());
            }
        }
        updateStatus();
    }
    
    @FXML
    private void close() {
        stage.close();
        if (onComplete != null) {
            onComplete.run();
        }
    }
    
    private void updateStatus() {
        int remaining = unmatchedList.size();
        statusLabel.setText(remaining + " unmatched images remaining");
        
        if (remaining == 0) {
            statusLabel.setText("All images matched!");
            statusLabel.setStyle("-fx-text-fill: green;");
        }
        
        // Count strong suggestions
        long strongMatches = unmatchedList.stream()
            .filter(UnmatchedImage::hasStrongSuggestion)
            .count();
        assignAllButton.setDisable(strongMatches == 0);
        assignAllButton.setText("Assign All Suggestions (" + strongMatches + ")");
    }
}
