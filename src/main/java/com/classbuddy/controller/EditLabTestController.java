package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.model.LabTest;
import com.classbuddy.service.LabTestService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.util.ViewTransitions;

import java.io.IOException;
import java.time.LocalDate;

public class EditLabTestController {

    @FXML private Label classroomNameLabel;
    @FXML private DatePicker testDatePicker;
    @FXML private TextField experimentField;
    @FXML private TextField teacherField;
    @FXML private TextArea criteriaArea;
    @FXML private Label messageLabel;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;

    private Classroom classroom;
    private LabTest labTest;

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroom != null && classroomNameLabel != null) classroomNameLabel.setText(classroom.getName());
    }

    public void setLabTest(LabTest labTest) { this.labTest = labTest; }

    public void loadData() {
        if (labTest != null) {
            if (labTest.getTestDate() != null) testDatePicker.setValue(labTest.getTestDate());
            experimentField.setText(labTest.getExperimentNumber());
            teacherField.setText(labTest.getTeacherName());
            criteriaArea.setText(labTest.getEvaluationCriteria() == null ? "" : labTest.getEvaluationCriteria());
        }
    }

    @FXML
    public void handleUpdate() {
        hideMessage();
        try {
            LocalDate d = testDatePicker.getValue();
            String exp = experimentField.getText().trim();
            String teacher = teacherField.getText().trim();
            String criteria = criteriaArea.getText().trim();

            if (d == null) { showError("Please select a test date"); return; }
            if (exp.isEmpty()) { showError("Please enter experiment number"); return; }
            if (teacher.isEmpty()) { showError("Please enter teacher name"); return; }

            boolean ok = LabTestService.updateLabTest(labTest.getId(), d, exp, teacher, criteria);
            if (ok) {
                showSuccess("Lab test updated successfully");
                new Thread(() -> { try { Thread.sleep(780); } catch (InterruptedException ignored) {};
                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else showError("Failed to update lab test");
        } catch (Exception e) { showError("Error: " + e.getMessage()); }
    }

    @FXML
    public void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Lab Test");
        alert.setHeaderText("Delete Lab Test?");
        alert.setContentText("Are you sure you want to delete this lab test? This action cannot be undone.");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean deleted = LabTestService.deleteLabTest(labTest.getId());
            if (deleted) {
                showSuccess("Lab test deleted successfully");
                new Thread(() -> { try { Thread.sleep(780);} catch (InterruptedException ignored) {};
                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else showError("Failed to delete lab test");
        }
    }

    @FXML
    public void goBack() {
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
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("success-message","error-message");
        messageLabel.getStyleClass().add("error-message");
        messageLabel.setVisible(true); messageLabel.setManaged(true);
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("success-message","error-message");
        messageLabel.getStyleClass().add("success-message");
        messageLabel.setVisible(true); messageLabel.setManaged(true);
    }

    private void hideMessage() { if (messageLabel==null) return; messageLabel.setVisible(false); messageLabel.setManaged(false); messageLabel.setText(""); }
}
