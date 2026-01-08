package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com.classbuddy.service.LabTestService;
import com.classbuddy.util.ViewTransitions;

import java.io.IOException;
import java.time.LocalDate;

public class AddLabTestController {
    @FXML
    private Label classroomNameLabel;
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

    public void setInitialTestDate(LocalDate date) {
        if (testDatePicker != null && date != null) {
            testDatePicker.setValue(date);
        }
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
            showSuccess("Lab test added successfully.");
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
            showError("Failed to add lab test");
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
            Stage stage = (Stage) testDatePicker.getScene().getWindow();
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
    public void goToAddCTQuiz() {
        navigateToAddScreen("/fxml/add-ctquiz.fxml");
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
            } else if (controller instanceof AddCTQuizController) {
                AddCTQuizController ctrl = (AddCTQuizController) controller;
                ctrl.setClassroom(classroom);
            } else if (controller instanceof AddNoticeController) {
                AddNoticeController ctrl = (AddNoticeController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            }

            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) testDatePicker.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) testDatePicker.getScene().getWindow();
        stage.close();
    }

    private void clearFields() {
        testDatePicker.setValue(null);
        experimentField.clear();
        teacherField.clear();
        criteriaArea.clear();
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
