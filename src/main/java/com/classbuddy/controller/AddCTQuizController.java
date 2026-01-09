package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com.classbuddy.service.CTQuizService;
import com.classbuddy.util.ViewTransitions;

import java.io.IOException;
import java.time.LocalDate;

public class AddCTQuizController {

    @FXML
    private Label classroomNameLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea syllabusArea;
    @FXML
    private DatePicker deadlinePicker;
    @FXML
    private Label errorLabel;

    private Classroom classroom;
    private int classroomId;
    private Runnable onSuccess;

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroom != null) {
            this.classroomId = classroom.getId();
            if (classroomNameLabel != null) {
                classroomNameLabel.setText(classroom.getName());
            }
        }
    }

    public void setClassroomId(int classroomId) {
        this.classroomId = classroomId;
    }

    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setInitialDeadline(LocalDate date) {
        if (deadlinePicker != null && date != null) {
            deadlinePicker.setValue(date);
        }
    }

    @FXML
    public void initialize() {
        // Auto-complete for quiz/test name; exclude date picker
        com.classbuddy.util.AutoCompleteUtil.bind(nameField,
                p -> com.classbuddy.service.SuggestionService.getSuggestions(
                        com.classbuddy.service.SuggestionService.FieldType.TEST_NAME, p, 8));
    }

    @FXML
    public void handleAdd() {
        String name = nameField.getText().trim();
        String syllabus = syllabusArea.getText().trim();
        LocalDate deadline = deadlinePicker.getValue();

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

        boolean success = CTQuizService.addCTQuiz(classroomId, name, syllabus, deadline);

        if (success) {
            // Record values for future suggestions
            com.classbuddy.service.SuggestionService.recordValue(
                    com.classbuddy.service.SuggestionService.FieldType.TEST_NAME, name);
            showSuccess("CT/Quiz added successfully.");
            clearFields();

            new Thread(() -> {
                try {
                    Thread.sleep(780);
                } catch (InterruptedException ignored) {
                }

                javafx.application.Platform.runLater(() -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                        closeWindow();
                    } else {
                        goBack();
                    }
                });
            }).start();
        } else {
            showError("Failed to add CT/Quiz");
        }
    }

    @FXML
    public void handleCancel() {
        if (classroom != null) {
            goBack();
        } else {
            closeWindow();
        }
    }

    @FXML
    public void goBack() {
        if (classroom == null) {
            closeWindow();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/classroom-detail.fxml"));
            Parent root = loader.load();

            ClassroomDetailController controller = loader.getController();
            controller.setClassroom(classroom);
            controller.setUser(LoginController.getCurrentUser());
            controller.loadData();

            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1366);
            stage.setMinHeight(800);
            stage.setWidth(1366);
            stage.setHeight(800);
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
    public void goToAddExam() {
        navigateToAddScreen("/fxml/add-exam.fxml");
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
            } else if (controller instanceof AddExamController) {
                AddExamController ctrl = (AddExamController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            } else if (controller instanceof AddLabTestController) {
                AddLabTestController ctrl = (AddLabTestController) controller;
                ctrl.setClassroom(classroom);
            } else if (controller instanceof AddNoticeController) {
                AddNoticeController ctrl = (AddNoticeController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            }

            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void clearFields() {
        nameField.clear();
        syllabusArea.clear();
        deadlinePicker.setValue(null);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().removeAll("error-message", "success-message");
        errorLabel.getStyleClass().add("error-message");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().removeAll("error-message", "success-message");
        errorLabel.getStyleClass().add("success-message");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
