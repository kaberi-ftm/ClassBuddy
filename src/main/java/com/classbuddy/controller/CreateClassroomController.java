package com.classbuddy.controller;

import com.classbuddy.util.Toast;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import com.classbuddy.util.ViewTransitions;
import java.io.IOException;

public class CreateClassroomController {
    @FXML
    private Label adminNameLabel;

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

    @FXML
    public void initialize() {
        if (admin == null) {
            admin = LoginController.getCurrentUser();
        }

        if (adminNameLabel != null && admin != null) {
            adminNameLabel.setText(admin.getUsername());
        }
    }

    /**
     * Set the current admin user
     */
    public void setAdmin(User admin) {
        this.admin = admin;

        if (adminNameLabel != null && admin != null) {
            adminNameLabel.setText(admin.getUsername());
        }
    }

    @FXML
    public void goToCreateClassroom() {
        // no-op (already here)
    }

    @FXML
    public void goToJoinClassroom() {
        navigateToView("/fxml/join-classroom.fxml");
    }

    @FXML
    public void goToCalendar() {
        navigateToView("/fxml/admin-calendar.fxml");
    }

    @FXML
    public void goToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(admin != null ? admin : LoginController.getCurrentUser(), "/fxml/create-classroom.fxml");

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) classNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            showError("Failed to load profile.");
        }
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 600);
            Stage stage = (Stage) classNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);

            LoginController.setCurrentUser(null);
        } catch (IOException e) {
            showError("Logout failed.");
        }
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
            showSuccess("Classroom created successfully.");
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
            Scene scene = new Scene(root, 1200, 800);

            Stage stage = (Stage) classNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error navigating back.");
        }
    }

    private void navigateToView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) classNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            showError("Navigation failed.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        Stage stage = (Stage) classNameField.getScene().getWindow();
        Toast.show(stage, message, Toast.Type.SUCCESS);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void clearFields() {
        classNameField. clear();
        sectionField. clear();
        departmentField. clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}