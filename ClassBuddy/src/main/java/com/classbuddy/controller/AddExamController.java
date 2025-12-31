package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.service.ExamService;
import java.time.LocalDate;
import java.time.LocalTime;

public class AddExamController {
    @FXML
    private TextField courseField;
    @FXML
    private ComboBox<String> examTypeComboBox;
    @FXML
    private DatePicker examDatePicker;
    @FXML
    private TextField examTimeField;
    @FXML
    private TextField roomField;
    @FXML
    private Label errorLabel;

    private int classroomId;
    private Runnable onSuccess;

    @FXML
    public void initialize() {
        // Populate exam type combo box
        examTypeComboBox.getItems().addAll("Mid", "Final", "Viva");
    }

    public void setClassroomId(int classroomId) {
        this.classroomId = classroomId;
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    public void handleAdd() {
        String course = courseField.getText().trim();
        String examType = examTypeComboBox.getValue();
        LocalDate examDate = examDatePicker.getValue();
        String timeStr = examTimeField.getText().trim();
        String room = roomField.getText().trim();

        // Validation
        if (course.isEmpty()) {
            showError("Please enter course name");
            return;
        }

        if (examType == null) {
            showError("Please select exam type");
            return;
        }

        if (examDate == null) {
            showError("Please select exam date");
            return;
        }

        if (timeStr.isEmpty()) {
            showError("Please enter exam time");
            return;
        }

        if (room.isEmpty()) {
            showError("Please enter room number");
            return;
        }

        try {
            LocalTime examTime = LocalTime.parse(timeStr);

            // Add to database
            boolean success = ExamService.addExam(
                    classroomId, course, examType, examDate, examTime, room
            );

            if (success) {
                showSuccess("Exam added successfully!");

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
                showError("Failed to add exam");
            }

        } catch (Exception e) {
            showError("Invalid time format. Use HH:MM (e.g., 09:00)");
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) courseField.getScene().getWindow();
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
