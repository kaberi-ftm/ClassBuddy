package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.model.Role;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import com.classbuddy.service.ScheduleExportService;
import com.classbuddy.service.ScheduleImportService;
import com.classbuddy.util.NavigationUtil;
import com.classbuddy.util.ViewTransitions;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Controller for Schedule Export/Import functionality
 * Admin-only feature for bulk schedule management
 */
public class ScheduleExportImportController {

    @FXML private ComboBox<Classroom> exportClassroomCombo;
    @FXML private TextField exportFilePathField;
    @FXML private Button exportBrowseButton;
    @FXML private Button exportButton;
    @FXML private Label exportStatusLabel;
    @FXML private ProgressIndicator exportProgress;

    @FXML private ComboBox<Classroom> importClassroomCombo;
    @FXML private TextField importFilePathField;
    @FXML private Button importBrowseButton;
    @FXML private Button importButton;
    @FXML private Label importStatusLabel;
    @FXML private ProgressIndicator importProgress;
    @FXML private TextArea importPreviewArea;

    private User currentUser;
    private List<Classroom> adminClassrooms;

    @FXML
    public void initialize() {
        // Initially hide progress indicators
        exportProgress.setVisible(false);
        importProgress.setVisible(false);

        hideStatus(exportStatusLabel);
        hideStatus(importStatusLabel);

        // Load admin's classrooms
        loadClassrooms();

        // Setup classroom display format
        exportClassroomCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Classroom item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getSection() + ")");
            }
        });
        exportClassroomCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Classroom item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getSection() + ")");
            }
        });

        importClassroomCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Classroom item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getSection() + ")");
            }
        });
        importClassroomCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Classroom item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getSection() + ")");
            }
        });
    }

    private void loadClassrooms() {
        currentUser = LoginController.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            showError("Unauthorized access. Admin privileges required.");
            return;
        }

        adminClassrooms = ClassroomService.getAdminClassrooms(currentUser.getId());
        ObservableList<Classroom> classrooms = FXCollections.observableArrayList(adminClassrooms);
        
        exportClassroomCombo.setItems(classrooms);
        importClassroomCombo.setItems(classrooms);

        if (!classrooms.isEmpty()) {
            exportClassroomCombo.getSelectionModel().selectFirst();
            importClassroomCombo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleExportBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Schedule Export");
        fileChooser.setInitialFileName("schedule_export.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        Stage stage = (Stage) exportBrowseButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            exportFilePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleExportSchedule() {
        Classroom selectedClassroom = exportClassroomCombo.getValue();
        String filePath = exportFilePathField.getText();

        // Validation
        if (selectedClassroom == null) {
            showError("Please select a classroom to export.");
            return;
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            showError("Please select an output file path.");
            return;
        }

        // Show progress
        exportProgress.setVisible(true);
        exportButton.setDisable(true);
        setStatus(exportStatusLabel, "Exporting...", "info-message");

        // Perform export in background
        new Thread(() -> {
            try {
                Path outputPath = Path.of(filePath);
                ScheduleExportService.ExportResult result = 
                    ScheduleExportService.exportScheduleToCSV(selectedClassroom.getId(), outputPath);

                // Update UI on success
                javafx.application.Platform.runLater(() -> {
                    exportProgress.setVisible(false);
                    exportButton.setDisable(false);
                    setStatus(
                        exportStatusLabel,
                        String.format("✓ Export successful! %d rows exported to:\n%s", result.getTotalRows(), result.getFilePath()),
                        "success-message"
                    );
                });

            } catch (IOException e) {
                // Update UI on error
                javafx.application.Platform.runLater(() -> {
                    exportProgress.setVisible(false);
                    exportButton.setDisable(false);
                    setStatus(exportStatusLabel, "✗ Export failed: " + e.getMessage(), "error-message");
                });
            }
        }).start();
    }

    @FXML
    private void handleImportBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Schedule File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        Stage stage = (Stage) importBrowseButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            importFilePathField.setText(file.getAbsolutePath());
            previewImportFile(file);
        }
    }

    private void previewImportFile(File file) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            StringBuilder preview = new StringBuilder();
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < 10) {
                preview.append(line).append("\n");
                lineCount++;
            }

            if (reader.readLine() != null) {
                preview.append("\n... (more rows not shown)");
            }

            importPreviewArea.setText(preview.toString());
        } catch (IOException e) {
            importPreviewArea.setText("Error reading file: " + e.getMessage());
        }
    }

    @FXML
    private void handleImportSchedule() {
        Classroom selectedClassroom = importClassroomCombo.getValue();
        String filePath = importFilePathField.getText();

        if (selectedClassroom == null) {
            showError("Please select a classroom to import into.");
            return;
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            showError("Please select an import file.");
            return;
        }

        importProgress.setVisible(true);
        importButton.setDisable(true);
        setStatus(importStatusLabel, "Importing...", "info-message");

        new Thread(() -> {
            try {
                ScheduleImportService.ImportResult result = ScheduleImportService.importSchedule(
                        selectedClassroom.getId(), currentUser.getId(), Path.of(filePath));

                javafx.application.Platform.runLater(() -> {
                    importProgress.setVisible(false);
                    importButton.setDisable(false);

                    if (!result.conflicts.isEmpty()) {
                        setStatus(
                            importStatusLabel,
                            "✗ Conflicts found:\n" + String.join("\n", result.conflicts),
                            "warning-message"
                        );
                        return;
                    }

                    if (!result.errors.isEmpty()) {
                        setStatus(
                            importStatusLabel,
                            "✗ Import failed:\n" + String.join("\n", result.errors),
                            "error-message"
                        );
                        return;
                    }

                    setStatus(importStatusLabel, "✓ Import complete. " + result.toString(), "success-message");
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    importProgress.setVisible(false);
                    importButton.setDisable(false);
                    setStatus(importStatusLabel, "✗ Import failed: " + e.getMessage(), "error-message");
                });
            }
        }).start();
    }

    private void hideStatus(Label label) {
        if (label == null) {
            return;
        }
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
        clearMessageStyles(label);
    }

    private void setStatus(Label label, String text, String messageStyleClass) {
        if (label == null) {
            return;
        }
        label.setText(text);
        label.setVisible(true);
        label.setManaged(true);
        clearMessageStyles(label);
        if (messageStyleClass != null && !messageStyleClass.isBlank()) {
            label.getStyleClass().add(messageStyleClass);
        }
    }

    private void clearMessageStyles(Label label) {
        label.getStyleClass().removeAll(
            "error-message",
            "success-message",
            "info-message",
            "warning-message"
        );
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) exportButton.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to return to dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @SuppressWarnings("unused")
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
