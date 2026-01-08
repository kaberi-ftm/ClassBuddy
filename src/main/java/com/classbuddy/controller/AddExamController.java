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
import com.classbuddy.util.TimeOptions;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

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
    private ComboBox<String> examTimeComboBox;
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
        examTypeComboBox.getItems().addAll("Mid", "Final", "Viva", "CT", "Quiz", "Lab Test");
        examTypeComboBox.setValue("Mid");

        examTimeComboBox.getItems().setAll(TimeOptions.defaultTimes());
        examTimeComboBox.setEditable(true);
    }

    public void loadData() {
        if (classroom != null) {
            classroomNameLabel.setText(classroom.getName());
        }
    }

    public void setInitialDate(LocalDate date) {
        if (examDatePicker != null && date != null) {
            examDatePicker.setValue(date);
        }
    }

    @FXML
    public void handleAddExam() {
        try {
            String courseName = courseNameField.getText().trim();
            String examType = examTypeComboBox.getValue();
            LocalDate examDate = examDatePicker.getValue();
            String room = roomField.getText().trim();
            String timeText = getComboText(examTimeComboBox);

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

            LocalTime examTime = TimeOptions.parseHHmm(timeText);

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
            showError("Invalid time format. Use h:mm AM/PM (e.g., 2:00 PM)");
            e.printStackTrace();
        }
    }

    private static String getComboText(ComboBox<String> comboBox) {
        if (comboBox == null) return "";
        String editor = comboBox.getEditor() != null ? comboBox.getEditor().getText() : null;
        if (editor != null && !editor.trim().isEmpty()) {
            return editor.trim();
        }
        String value = comboBox.getValue();
        return value != null ? value.trim() : "";
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

            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToAddRoutine() {
        navigateToAddScreen("/fxml/add-routine.fxml");
    }

    @FXML
    public void goToAddCTQuiz() {
        navigateToAddScreen("/fxml/add-ctquiz.fxml");
    }

    @FXML
    public void goToAddLabTest() {
        navigateToAddScreen("/fxml/add-labtest.fxml");
    }

    @FXML
    public void goToAddNotice() {
        navigateToAddScreen("/fxml/add-notice.fxml");
    }

    private void navigateToAddScreen(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AddRoutineController) {
                AddRoutineController ctrl = (AddRoutineController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            } else if (controller instanceof AddCTQuizController) {
                AddCTQuizController ctrl = (AddCTQuizController) controller;
                ctrl.setClassroom(classroom);
            } else if (controller instanceof AddLabTestController) {
                AddLabTestController ctrl = (AddLabTestController) controller;
                ctrl.setClassroom(classroom);
            } else if (controller instanceof AddNoticeController) {
                AddNoticeController ctrl = (AddNoticeController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            }

            Scene scene = new Scene(root, 1600, 900);
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
        examTimeComboBox.setValue(null);
        if (examTimeComboBox.getEditor() != null) {
            examTimeComboBox.getEditor().clear();
        }
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