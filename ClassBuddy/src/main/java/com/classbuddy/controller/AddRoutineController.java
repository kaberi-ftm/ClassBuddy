package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.service.RoutineService;
import java.time.LocalTime;

public class AddRoutineController {
    @FXML
    private ComboBox<String> dayComboBox;
    @FXML
    private TextField periodField;
    @FXML
    private TextField courseField;
    @FXML
    private TextField teacherField;
    @FXML
    private TextField roomField;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField endTimeField;
    @FXML
    private Label errorLabel;

    private int classroomId;
    private Runnable onSuccess;

    @FXML
    public void initialize() {
        // Populate day combo box
        dayComboBox.getItems().addAll(
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
        );
    }

    public void setClassroomId(int classroomId) {
        this.classroomId = classroomId;
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    @FXML
    public void handleAdd() {
        String day = dayComboBox.getValue();
        String periodStr = periodField.getText().trim();
        String course = courseField.getText().trim();
        String teacher = teacherField.getText().trim();
        String room = roomField.getText().trim();
        String startTime = startTimeField.getText().trim();
        String endTime = endTimeField.getText().trim();

        // Validation
        if (day == null) {
            showError("Please select a day");
            return;
        }

        if (periodStr.isEmpty()) {
            showError("Please enter period number");
            return;
        }

        if (course.isEmpty()) {
            showError("Please enter course name");
            return;
        }

        if (startTime.isEmpty() || endTime.isEmpty()) {
            showError("Please enter start and end times");
            return;
        }

        try {
            int period = Integer.parseInt(periodStr);
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);

            if (end.isBefore(start)) {
                showError("End time must be after start time");
                return;
            }

            // Add to database
            boolean success = RoutineService.addRoutine(
                    classroomId, day, period, course, teacher, room, start, end
            );

            if (success) {
                showSuccess("Routine added successfully!");

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
                showError("Failed to add routine");
            }

        } catch (NumberFormatException e) {
            showError("Period must be a number");
        } catch (Exception e) {
            showError("Invalid time format. Use HH:MM (e.g., 09:00)");
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) dayComboBox.getScene().getWindow();
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
