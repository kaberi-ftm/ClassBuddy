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

        System.out.println("\n🔍 Attempting login for:  " + usernameOrEmail);

        // Authenticate user from database
        User user = AuthService.loginUser(usernameOrEmail, password);

        if (user != null) {
            // Login successful
            currentUser = user;

            // Update last login time
            AuthService.updateLastLoginTime(user.getId());

            showSuccess("Login successful! Welcome, " + user.getUsername() + "!");

            System.out.println("✅ User logged in: " + user.getUsername() + " (" + user.getRole() + ")");

            // Navigate to appropriate dashboard based on role
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        if (user.getRole().name().equals("ADMIN")) {
                            navigateToAdminDashboard();
                        } else {
                            navigateToStudentDashboard();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            // Login failed
            showError("❌ Invalid username/email or password.");
            passwordField.clear();
        }
    }

    /**
     * Navigate to admin dashboard
     */
    private void navigateToAdminDashboard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1000, 700);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading dashboard.");
        }
    }

    /**
     * Navigate to student dashboard
     */
    private void navigateToStudentDashboard() {
        System.out.println("TODO: Create student dashboard");
        showError("Student dashboard coming soon!");
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

    // Get current logged-in user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Set current user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}