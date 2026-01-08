package com.classbuddy. controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene. control.*;
import javafx.stage.Stage;
import com.classbuddy.model.Role;
import com.classbuddy.service.AuthService;
import com.classbuddy.util.NavigationUtil;
import com.classbuddy.util.ViewTransitions;
import com.classbuddy.util.Toast;
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
        clearValidation();

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        boolean isAdmin = adminRadio.isSelected();

        // Validation: Check empty fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            if (username.isEmpty()) markInvalid(usernameField);
            if (email.isEmpty()) markInvalid(emailField);
            if (password.isEmpty()) markInvalid(passwordField);
            if (confirmPassword.isEmpty()) markInvalid(confirmPasswordField);
            showError("All fields are required.");
            return;
        }

        // Validation: Username length
        if (username.length() < 3) {
            markInvalid(usernameField);
            showError("Username must be at least 3 characters long.");
            return;
        }

        // Validation: Email format
        if (!isValidEmail(email)) {
            markInvalid(emailField);
            showError("Please enter a valid email address.");
            return;
        }


        if (!password.equals(confirmPassword)) {
            markInvalid(passwordField);
            markInvalid(confirmPasswordField);
            showError("Passwords do not match.");
            return;
        }


        if (password.length() < 6) {
            markInvalid(passwordField);
            showError("Password must be at least 6 characters long.");
            return;
        }

        if (!AuthService.isUsernameUnique(username)) {
            markInvalid(usernameField);
            showError("Username already exists. Please choose another.");
            return;
        }

        if (!AuthService.isEmailUnique(email)) {
            markInvalid(emailField);
            showError("Email already registered. Please use another or login.");
            return;
        }

        // Attempt registration
        Role role = isAdmin ? Role.ADMIN : Role.STUDENT;
        boolean registered = AuthService.registerUser(username, email, password, role);

        if (registered) {
            showSuccess("Registration successful! Please login with your credentials.");
            clearValidation();
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

    private void clearValidation() {
        if (usernameField != null) usernameField.getStyleClass().remove("field-invalid");
        if (emailField != null) emailField.getStyleClass().remove("field-invalid");
        if (passwordField != null) passwordField.getStyleClass().remove("field-invalid");
        if (confirmPasswordField != null) confirmPasswordField.getStyleClass().remove("field-invalid");
    }

    private void markInvalid(Control control) {
        if (control == null) {
            return;
        }
        if (!control.getStyleClass().contains("field-invalid")) {
            control.getStyleClass().add("field-invalid");
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

            Stage stage = (Stage) usernameField.getScene().getWindow();
            if (stage != null) {
                NavigationUtil.applyLoginScene(stage, root);
                ViewTransitions.fadeIn(root);
            }
        } catch (IOException e) {
            System.err.println("Error loading login page: " + e.getMessage());
            showError("Error loading login page.");
        } catch (NullPointerException e) {
            System.err.println("Cannot navigate - window not available: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().removeAll("success-message");
        if (!errorLabel.getStyleClass().contains("error-message")) {
            errorLabel.getStyleClass().add("error-message");
        }
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().removeAll("error-message");
        if (!errorLabel.getStyleClass().contains("success-message")) {
            errorLabel.getStyleClass().add("success-message");
        }
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        Stage stage = (Stage) usernameField.getScene().getWindow();
        Toast.show(stage, message, Toast.Type.SUCCESS);
    }

    private void clearFields() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        studentRadio.setSelected(true);
    }
}