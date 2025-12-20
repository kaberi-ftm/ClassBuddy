package com.classbuddy.controller;

import javafx. fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene. Scene;
import javafx.scene. control. Label;
import javafx.scene. control.TextField;
import javafx.stage.Stage;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import java.io.IOException;

public class CreateClassroomController {
    @FXML
    private TextField classNameField;
    @FXML
    private TextField sectionField;
    @FXML
    private TextField departmentField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField confirmPasswordField;
    @FXML
    private Label errorLabel;

    private User admin;

    /**
     * Set the current admin user
     */
    public void setAdmin(User admin) {
        this.admin = admin;
    }

    @FXML
    public void handleCreateClassroom() {
        String name = classNameField.getText().trim();
        String section = sectionField.getText().trim();
        String department = departmentField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (name.isEmpty() || section.isEmpty() ||
                department.isEmpty() || password.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (password.length() < 4) {
            showError("Classroom password must be at least 4 characters.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        // Check if classroom name is unique
        if (!ClassroomService.isClassroomNameUnique(admin.getId(), name)) {
            showError("You already have a classroom with this name.");
            return;
        }

        // Create classroom
        boolean created = ClassroomService.createClassroom(
                admin.getId(), name, section, department, password
        );

        if (created) {
            showSuccess("Classroom created successfully!");
            clearFields();

            // Navigate back to dashboard after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::goBackToDashboard);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("Error creating classroom. Please try again.");
        }
    }

    @FXML
    public void goBackToDashboard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1000, 700);

            Stage stage = (Stage) classNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error navigating back.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #27ae60;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearFields() {
        classNameField. clear();
        sectionField. clear();
        departmentField. clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}