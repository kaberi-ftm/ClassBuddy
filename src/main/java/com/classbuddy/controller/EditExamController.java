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
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
    private TextField examTimeField;
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
        examTypeComboBox.getItems().addAll("Mid", "Final", "Viva");
        examTypeComboBox.setValue("Mid");
    }

    public void loadData() {
        if (classroom != null) {
            classroomNameLabel.setText(classroom.getName());
        }

        if (exam != null) {
            courseNameField.setText(exam.getCourseName());
            examTypeComboBox.setValue(exam.getExamType());
            examDatePicker.setValue(exam.getExamDate());
            examTimeField.setText(exam.getExamTime().format(DateTimeFormatter.ofPattern("HH:mm")));
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

            // Update exam
            boolean updated = ExamService.updateExam(
                    exam.getId(), courseName, examType, examDate, examTime, room
            );

            if (updated) {
                showSuccess("Exam updated successfully");

                new Thread(() -> {
                    try {
                        Thread.sleep(900);
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
                        Thread.sleep(900);
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

            Scene scene = new Scene(root, 1200, 800);
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

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: -error;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: -success;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
