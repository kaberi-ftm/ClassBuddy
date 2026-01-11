package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.model.CTQuiz;
import com.classbuddy.service.CTQuizService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.util.ViewTransitions;

import java.io.IOException;
import java.time.LocalDate;

public class EditCTQuizController {

    @FXML
    private Label classroomNameLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea syllabusArea;
    @FXML
    private DatePicker deadlinePicker;
    @FXML
    private Label messageLabel;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private Classroom classroom;
    private CTQuiz ctQuiz;

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroom != null && classroomNameLabel != null) {
            classroomNameLabel.setText(classroom.getName());
        }
    }

    public void setCtQuiz(CTQuiz ctQuiz) {
        this.ctQuiz = ctQuiz;
    }

    public void loadData() {
        if (ctQuiz != null) {
            nameField.setText(ctQuiz.getName());
            syllabusArea.setText(ctQuiz.getSyllabus() == null ? "" : ctQuiz.getSyllabus());
            if (ctQuiz.getDeadline() != null) deadlinePicker.setValue(ctQuiz.getDeadline());
        }
    }

    @FXML
    public void initialize() {
        // nothing special
    }

    @FXML
    public void handleUpdate() {
        // clear
        hideMessage();

        try {
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

            boolean ok = CTQuizService.updateCTQuiz(ctQuiz.getId(), name, syllabus, deadline);
            if (ok) {
                showSuccess("CT/Quiz updated successfully");
                new Thread(() -> {
                    try { Thread.sleep(780); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else {
                showError("Failed to update CT/Quiz");
            }
        } catch (Exception e) {
            showError("Error updating: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete CT/Quiz");
        alert.setHeaderText("Delete CT/Quiz?");
        alert.setContentText("Are you sure you want to delete this CT/Quiz? This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean deleted = CTQuizService.deleteCTQuiz(ctQuiz.getId());
            if (deleted) {
                showSuccess("CT/Quiz deleted successfully");
                new Thread(() -> {
                    try { Thread.sleep(780); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else {
                showError("Failed to delete CT/Quiz");
            }
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
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("success-message", "error-message");
        messageLabel.getStyleClass().add("error-message");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("success-message", "error-message");
        messageLabel.getStyleClass().add("success-message");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage() {
        if (messageLabel == null) return;
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        messageLabel.setText("");
    }
}
