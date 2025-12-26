package com.classbuddy. controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene. control.*;
import javafx.stage.Stage;
import com.classbuddy.model.Role;
import com.classbuddy.service.AuthService;
import java.io.IOException;

public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private RadioButton studentRadio;
    @FXML
    private RadioButton adminRadio;
    @FXML
    private Label errorLabel;

    @FXML
    public void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        boolean isAdmin = adminRadio.isSelected();

        // Validation: Check empty fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        // Validation: Username length
        if (username.length() < 3) {
            showError("Username must be at least 3 characters long.");
            return;
        }

        // Validation: Email format
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }


        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }


        if (password.length() < 6) {
            showError("Password must be at least 6 characters long.");
            return;
        }

        if (!AuthService.isUsernameUnique(username)) {
            showError("Username already exists. Please choose another.");
            return;
        }

        if (!AuthService.isEmailUnique(email)) {
            showError("Email already registered. Please use another or login.");
            return;
        }

        // Attempt registration
        Role role = isAdmin ? Role.ADMIN : Role.STUDENT;
        boolean registered = AuthService.registerUser(username, email, password, role);

        if (registered) {
            showSuccess("Registration successful! Please login with your credentials.");
            clearFields();

            // Auto-navigate to login after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::goToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("Registration failed. Please try again or contact support.");
        }
    }


    private boolean isValidEmail(String email) {
        return email. matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @FXML
    public void goToLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 900, 600);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading login page: " + e.getMessage());
            showError("Error loading login page.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel. setStyle("-fx-text-fill: green;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearFields() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        studentRadio.setSelected(true);
    }
}