package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene. control.*;
import javafx.stage.Stage;
import com.classbuddy.model.User;
import com.classbuddy.service.AuthService;
import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private CheckBox rememberMeCheckbox;

    // Store current logged-in user globally
    private static User currentUser;

    @FXML
    public void handleLogin() {
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showError("Please enter username/email and password.");
            return;
        }

        // Authenticate user from database
        User user = AuthService.loginUser(usernameOrEmail, password);

        if (user != null) {
            // Login successful
            currentUser = user;
            showSuccess("Login successful! Welcome, " + user.getUsername() + "!");

            // TODO: Navigate to appropriate dashboard based on role
            System.out.println("User logged in: " + user.getUsername() + " (" + user.getRole() + ")");

            // For now, just clear fields
            usernameField.clear();
            passwordField.clear();
        } else {
            // Login failed
            showError("Invalid username/email or password.");
            passwordField.clear();
        }
    }

    @FXML
    public void goToRegister() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 900, 600);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading register page.");
        }
    }

    @FXML
    public void handleForgotPassword() {
        showError("Password recovery coming soon.");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    // Get current logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Set current user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}