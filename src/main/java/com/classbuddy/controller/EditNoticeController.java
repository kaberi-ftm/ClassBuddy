package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.model.Notice;
import com.classbuddy.service.NoticeService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import com.classbuddy.util.ViewTransitions;

import java.io.IOException;

public class EditNoticeController {

    @FXML
    private Label classroomNameLabel;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    
    @FXML
    private TextArea contentArea;
    
    @FXML
    private CheckBox pinnedCheckBox;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Label successLabel;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Button deleteButton;
    
    private Classroom classroom;
    private Notice notice;
    
    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroomNameLabel != null && classroom != null) {
            classroomNameLabel.setText(classroom.getName());
        }
    }
    
    public void setNotice(Notice notice) {
        this.notice = notice;
    }
    
    public void loadData() {
        if (notice != null) {
            titleField.setText(notice.getTitle());
            categoryComboBox.setValue(notice.getCategory());
            contentArea.setText(notice.getContent());
            pinnedCheckBox.setSelected(notice.isPinned());
        }
    }
    
    @FXML
    public void initialize() {
        // Initialize category dropdown
        categoryComboBox.getItems().addAll("Routine", "Exam", "CT", "General");
        categoryComboBox.setValue("General");
    }
    
    @FXML
    public void handleUpdate() {
        // Clear previous messages
        hideMessages();
        
        // Validate inputs
        String title = titleField.getText().trim();
        String category = categoryComboBox.getValue();
        String content = contentArea.getText().trim();
        
        if (title.isEmpty()) {
            showError("Please enter a notice title");
            return;
        }
        
        if (category == null || category.isEmpty()) {
            showError("Please select a category");
            return;
        }
        
        if (content.isEmpty()) {
            showError("Please enter notice content");
            return;
        }
        
        try {
            // Update notice in database
            boolean success = NoticeService.updateNotice(
                    notice.getId(),
                    title,
                    content,
                    category
            );
            
            if (success) {
                showSuccess("Notice updated successfully");
                
                new Thread(() -> {
                    try {
                        Thread.sleep(900);
                    } catch (InterruptedException ignored) {
                    }
                    
                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else {
                showError("Failed to update notice. Please try again.");
            }
        } catch (Exception e) {
            showError("Error updating notice: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Notice");
        alert.setHeaderText("Delete Notice?");
        alert.setContentText("Are you sure you want to delete this notice? This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean deleted = NoticeService.deleteNotice(notice.getId());

            if (deleted) {
                showSuccess("Notice deleted successfully");

                new Thread(() -> {
                    try {
                        Thread.sleep(900);
                    } catch (InterruptedException ignored) {
                    }

                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else {
                showError("Failed to delete notice");
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
    public void goToAddExam() {
        navigateToAddScreen("/fxml/add-exam.fxml");
    }

    @FXML
    public void goToAddCTQuiz() {
        navigateToAddScreen("/fxml/add-ctquiz.fxml");
    }

    @FXML
    public void goToAddLabTest() {
        navigateToAddScreen("/fxml/add-labtest.fxml");
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
            } else if (controller instanceof AddLabTestController) {
                AddLabTestController ctrl = (AddLabTestController) controller;
                ctrl.setClassroom(classroom);
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
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }
    
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    private void hideMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }
}
