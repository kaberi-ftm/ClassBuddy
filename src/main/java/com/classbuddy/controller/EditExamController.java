package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import com.classbuddy.util.ViewTransitions;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com.classbuddy.model.Exam;
import com.classbuddy.service.ExamService;
import com.classbuddy.util.TimeOptions;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class EditExamController {
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
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private Classroom classroom;
    private Exam exam;

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
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

        if (exam != null) {
            courseNameField.setText(exam.getCourseName());
            examTypeComboBox.setValue(exam.getExamType());
            examDatePicker.setValue(exam.getExamDate());
            examTimeComboBox.setValue(TimeOptions.format12h(exam.getExamTime()));
            roomField.setText(exam.getRoom() != null ? exam.getRoom() : "");
        }
    }

    @FXML
    public void handleUpdateExam() {
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

            if (timeText.isEmpty()) {
                showError("Exam time is required");
                return;
            }

            LocalTime examTime = TimeOptions.parseHHmm(timeText);

            // Update exam
            boolean updated = ExamService.updateExam(
                    exam.getId(), courseName, examType, examDate, examTime, room
            );

            if (updated) {
                showSuccess("Exam updated successfully");

                new Thread(() -> {
                    try {
                        Thread.sleep(780);
                    } catch (InterruptedException ignored) {
                    }

                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else {
                showError("Failed to update exam");
            }

        } catch (Exception e) {
            showError("Invalid input: " + e.getMessage());
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
    public void handleDeleteExam() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Exam");
        alert.setHeaderText("Delete Exam?");
        alert.setContentText("Are you sure you want to delete this exam? This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean deleted = ExamService.deleteExam(exam.getId());

            if (deleted) {
                showSuccess("Exam deleted successfully");

                new Thread(() -> {
                    try {
                        Thread.sleep(780);
                    } catch (InterruptedException ignored) {
                    }

                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else {
                showError("Failed to delete exam");
            }
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

            Scene scene = new Scene(root, 1366, 800);
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

            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        setMessage(msg, "error-message");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void showSuccess(String msg) {
        setMessage(msg, "success-message");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void setMessage(String msg, String messageStyleClass) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll(
            "error-message",
            "success-message",
            "info-message",
            "warning-message"
        );
        if (messageStyleClass != null && !messageStyleClass.isBlank()) {
            messageLabel.getStyleClass().add(messageStyleClass);
        }
    }
}
