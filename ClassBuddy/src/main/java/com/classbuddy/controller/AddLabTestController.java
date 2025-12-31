package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.service.LabTestService;
import java.time.LocalDate;

public class AddLabTestController {
    @FXML
    private DatePicker testDatePicker;
    @FXML
    private TextField experimentField;
    @FXML
    private TextField teacherField;
    @FXML
    private TextArea criteriaArea;
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
        LocalDate testDate = testDatePicker.getValue();
        String experimentNumber = experimentField.getText().trim();
        String teacherName = teacherField.getText().trim();
        String criteria = criteriaArea.getText().trim();

        // Validation
        if (testDate == null) {
            showError("Please select a test date");
            return;
        }

        if (experimentNumber.isEmpty()) {
            showError("Please enter experiment number");
            return;
        }

        if (teacherName.isEmpty()) {
            showError("Please enter teacher name");
            return;
        }

        // Add to database
        boolean success = LabTestService.addLabTest(
                classroomId, testDate, experimentNumber, teacherName, criteria
        );

        if (success) {
            showSuccess("Lab test added successfully!");
            
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
            showError("Failed to add lab test");
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) testDatePicker.getScene().getWindow();
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
