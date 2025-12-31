package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.service.CTQuizService;
import java.time.LocalDate;

public class AddCTQuizController {
    @FXML
    private TextField nameField;
    @FXML
    private TextArea syllabusArea;
    @FXML
    private DatePicker deadlinePicker;
    @FXML
    private Label errorLabel;

    private int classroomId;
    private Runnable onSuccess;

    public void setClassroomId(int classroomId) {
        this.classroomId = classroomId;
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    public void handleAdd() {
        String name = nameField.getText().trim();
        String syllabus = syllabusArea.getText().trim();
        LocalDate deadline = deadlinePicker.getValue();

        // Validation
        if (name.isEmpty()) {
            showError("Please enter CT/Quiz name");
            return;
        }

        if (deadline == null) {
            showError("Please select a deadline");
            return;
        }

        if (deadline.isBefore(LocalDate.now())) {
            showError("Deadline cannot be in the past");
            return;
        }

        // Add to database
        boolean success = CTQuizService.addCTQuiz(classroomId, name, syllabus, deadline);

        if (success) {
            showSuccess("CT/Quiz added successfully!");
            
            // Call callback after short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                        closeWindow();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("Failed to add CT/Quiz");
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #27ae60;");
        errorLabel.setVisible(true);
    }
}
