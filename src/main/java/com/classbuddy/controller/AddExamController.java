package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import com.classbuddy.util.ViewTransitions;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com.classbuddy.service.ExamService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AddExamController {
    @FXML
    private Label classroomNameLabel;
    @FXML
    private TextField courseNameField;
    @FXML
    private ComboBox<String> examTypeComboBox;
    @FXML
    private DatePicker examDatePicker;
    @FXML
    private TextField examTimeField;
    @FXML
    private TextField roomField;
    @FXML
    private Label messageLabel;

    private Classroom classroom;
    private Runnable onSuccessCallback;

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }



    public void setOnSuccess(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    @FXML
    public void initialize() {
        // Initialize exam types
        examTypeComboBox.getItems().addAll("Mid", "Final", "Viva");
        examTypeComboBox.setValue("Mid");
    }

    public void loadData() {
        if (classroom != null) {
            classroomNameLabel.setText(classroom.getName());
        }
    }

    @FXML
    public void handleAddExam() {
        try {
            String courseName = courseNameField.getText().trim();
            String examType = examTypeComboBox.getValue();
            LocalDate examDate = examDatePicker.getValue();
            String room = roomField.getText().trim();
            String timeText = examTimeField.getText().trim();

            // Validation
            if (courseName.isEmpty()) {
                showError("Course name is required");
                return;
            }

            if (examDate == null) {
                showError("Please select exam date");
                return;
            }

            if (examDate.isBefore(LocalDate.now())) {
                showError("Exam date cannot be in the past");
                return;
            }

            if (timeText.isEmpty()) {
                showError("Exam time is required");
                return;
            }

            // Validate time format before parsing
            if (!timeText.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                showError("Invalid time format. Use HH:mm (e.g., 14:00)");
                return;
            }

            // Parse time
            LocalTime examTime = LocalTime.parse(timeText,
                    DateTimeFormatter.ofPattern("HH:mm"));

            // Add exam
            boolean added = ExamService.addExam(
                    classroom.getId(), courseName, examType,
                    examDate, examTime, room
            );

            if (added) {
                showSuccess();
                clearFields();

                new Thread(() -> {
                    try {
                        Thread.sleep(900);
                    } catch (InterruptedException ignored) {
                    }

                    javafx.application.Platform.runLater(() -> {
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
                            stage.close();
                        } else {
                            goBack();
                        }
                    });
                }).start();
            } else {
                showError("Failed to add exam");
            }

        } catch (Exception e) {
            showError("Invalid time format. Use HH:mm (e.g., 14:00)");
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/classroom-detail.fxml")
            );
            Parent root = loader.load();

            ClassroomDetailController controller = loader.getController();
            controller.setClassroom(classroom);
            controller.setUser(LoginController.getCurrentUser());
            controller.loadData();

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        courseNameField.clear();
        examDatePicker.setValue(null);
        examTimeField.clear();
        roomField.clear();
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: -error;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void showSuccess() {
        messageLabel.setText("Exam added successfully");
        messageLabel.setStyle("-fx-text-fill: -success;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}