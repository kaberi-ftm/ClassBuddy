package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.model.User;
import com.classbuddy.service.AuthService;
import com.classbuddy.util.ViewTransitions;
import com.classbuddy.util.Toast;
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

    static User currentUser;

    @FXML
    public void handleLogin() {
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText();

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            showError("Please enter username/email and password.");
            return;
        }

        System.out.println("Attempting login for: " + usernameOrEmail);

        User user = AuthService.loginUser(usernameOrEmail, password);

        if (user != null) {
            currentUser = user;
            AuthService.updateLastLoginTime(user.getId());
            showSuccess("Login successful! Welcome, " + user.getUsername() + "!");

            System.out.println("User logged in: " + user.getUsername() + " (" + user.getRole() + ")");

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
            showError("Invalid username/email or password.");
            passwordField.clear();
        }
    }

    private void navigateToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            
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
            System.err.println("Failed to load admin dashboard: " + e.getMessage());
            e.printStackTrace();
            showError("Could not load dashboard.");
        }
    }

    private void navigateToStudentDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student-dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 800);
            
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
            System.err.println("Failed to load student dashboard: " + e.getMessage());
            e.printStackTrace();
            showError("Could not load dashboard.");
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
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading register page.");
        }
    }

    @FXML
    public void handleForgotPassword() {
        showError("Password recovery feature coming soon.");
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

        Stage stage = (Stage) usernameField.getScene().getWindow();
        Toast.show(stage, message, Toast.Type.SUCCESS);
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

}