package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.model.User;
import com.classbuddy.service.NoticeService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.util.ViewTransitions;

import java.io.IOException;

public class AddNoticeController {

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
    
    private Classroom classroom;
    
    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroomNameLabel != null && classroom != null) {
            classroomNameLabel.setText(classroom.getName());
        }
    }
    
    public void loadData() {
        // Set default category
        if (categoryComboBox != null && categoryComboBox.getItems().size() > 0) {
            categoryComboBox.getSelectionModel().selectFirst();
        }
    }
    
    @FXML
    public void initialize() {
        // Initialize will be called automatically by FXMLLoader
    }
    
    @FXML
    public void handleAdd() {
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
        
        if (classroom == null) {
            showError("Classroom not set. Please try again.");
            return;
        }
        
        try {
            // Get current user ID
            User currentUser = LoginController.getCurrentUser();
            if (currentUser == null) {
                showError("User session expired. Please log in again.");
                return;
            }
            
            int createdBy = currentUser.getId();
            
            // Post notice to database
            boolean success = NoticeService.postNotice(
                classroom.getId(), 
                title, 
                content, 
                category, 
                createdBy
            );
            
            if (success) {
                // Handle pinning if checkbox is selected
                if (pinnedCheckBox.isSelected()) {
                    // Note: You may need to get the notice ID to pin it
                    // For now, we'll just show success
                }
                
                showSuccess("Notice posted successfully.");
                
                // Clear fields after 1.5 second delay, then close
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    javafx.application.Platform.runLater(this::handleCancel);
                }).start();
            } else {
                showError("Failed to post notice. Please try again.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error posting notice: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleCancel() {
        goBack();
    }

    @FXML
    public void goBack() {
        if (classroom == null) {
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/classroom-detail.fxml"));
            Parent root = loader.load();

            ClassroomDetailController controller = loader.getController();
            controller.setClassroom(classroom);
            controller.setUser(LoginController.getCurrentUser());
            controller.loadData();

            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1600);
            stage.setMinHeight(900);
            stage.setWidth(1600);
            stage.setHeight(900);
            stage.centerOnScreen();
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

            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) titleField.getScene().getWindow();
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
    }
    
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        successLabel.setManaged(true);
    }
    
    private void hideMessages() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        successLabel.setVisible(false);
        successLabel.setManaged(false);
    }
}
