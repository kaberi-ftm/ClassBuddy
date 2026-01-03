package com.classbuddy.controller;

import com.classbuddy.util.Toast;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import com.classbuddy.model. Classroom;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import com.classbuddy.util.ViewTransitions;
import java.io.IOException;

public class JoinClassroomController {
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Button createClassroomNavBtn;

    @FXML
    private TextField rollNumberField;
    @FXML
    private PasswordField classroomPasswordField;
    @FXML
    private Label errorLabel;

    private User currentUser;

    @FXML
    public void initialize() {
        if (currentUser == null) {
            currentUser = LoginController.getCurrentUser();
        }

        if (currentUser != null) {
            if (userNameLabel != null) userNameLabel.setText(currentUser.getUsername());
            if (userRoleLabel != null) userRoleLabel.setText(currentUser.getRole().name());
        }

        if (createClassroomNavBtn != null) {
            boolean isAdmin = currentUser != null && currentUser.getRole() != null && currentUser.getRole().name().equals("ADMIN");
            createClassroomNavBtn.setVisible(isAdmin);
            createClassroomNavBtn.setManaged(isAdmin);
        }
    }

    /**
     * Set the current admin user
     */
    public void setAdmin(User admin) {
        this.currentUser = admin;

        if (currentUser != null) {
            if (userNameLabel != null) userNameLabel.setText(currentUser.getUsername());
            if (userRoleLabel != null) userRoleLabel.setText(currentUser.getRole().name());
        }

        if (createClassroomNavBtn != null) {
            boolean isAdmin = currentUser != null && currentUser.getRole() != null && currentUser.getRole().name().equals("ADMIN");
            createClassroomNavBtn.setVisible(isAdmin);
            createClassroomNavBtn.setManaged(isAdmin);
        }
    }

    @FXML
    public void goToJoinClassroom() {
        // no-op (already here)
    }

    @FXML
    public void goToCreateClassroom() {
        navigateToView("/fxml/create-classroom.fxml");
    }

    @FXML
    public void goToCalendar() {
        User u = currentUser != null ? currentUser : LoginController.getCurrentUser();
        String fxml = (u != null && u.getRole() != null && u.getRole().name().equals("ADMIN"))
                ? "/fxml/admin-calendar.fxml"
                : "/fxml/student-calendar.fxml";
        navigateToView(fxml);
    }

    @FXML
    public void goToProfile() {
        try {
            User u = currentUser != null ? currentUser : LoginController.getCurrentUser();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(u, "/fxml/join-classroom.fxml");

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) rollNumberField.getScene().getWindow();
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
            Stage stage = (Stage) rollNumberField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);

            LoginController.setCurrentUser(null);
        } catch (IOException e) {
            showError("Logout failed.");
        }
    }

    @FXML
    public void handleJoinClassroom() {
        String rollNumber = rollNumberField.getText().trim();
        String password = classroomPasswordField.getText();

        if (rollNumber.isEmpty() || password.isEmpty()) {
            showError("Please enter roll number and classroom password.");
            return;
        }

        // Find classroom by password
        Classroom classroom = findClassroomByPassword(password);

        if (classroom == null) {
            showError("Classroom not found. Invalid password.");
            return;
        }

        // Check if roll is allowed in this classroom
        if (!ClassroomService.isRollAllowedInClassroom(classroom. getId(), rollNumber)) {
            showError("Your roll number is not registered for this classroom.");
            return;
        }

        // Check if admin already joined
        User u = currentUser != null ? currentUser : LoginController.getCurrentUser();
        if (u != null && ClassroomService.isStudentInClassroom(classroom.getId(), u.getId())) {
            showError("You already joined this classroom.");
            return;
        }

        // Add admin to classroom
        boolean added = u != null && ClassroomService.addStudentToClassroom(classroom.getId(), u.getId(), rollNumber);

        if (added) {
            showSuccess("Joined classroom: " + classroom.getName());
            clearFields();

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::goBackToDashboard);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("Error joining classroom.  Please try again.");
        }
    }

    /**
     * Find classroom by password (brute force - check all classrooms)
     */
    private Classroom findClassroomByPassword(String password) {
        try (java.sql.Connection conn = com.classbuddy.util.DatabaseUtil.getConnection()) {
            String sql = "SELECT * FROM classroom";
            try (java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String hashedPassword = rs.getString("password_hash");
                    if (com.classbuddy.util. PasswordHasher.verifyPassword(password, hashedPassword)) {
                        return new Classroom(
                                rs.getInt("id"),
                                rs.getInt("admin_id"),
                                rs. getString("name"),
                                rs.getString("section"),
                                rs.getString("department"),
                                hashedPassword,
                                rs.getTimestamp("created_at").toLocalDateTime()
                        );
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error finding classroom:  " + e.getMessage());
        }

        return null;
    }

    @FXML
    public void goBackToDashboard() {
        try {
            User current = currentUser != null ? currentUser : LoginController.getCurrentUser();
            String dashboardFxml = (current != null && current.getRole().name().equals("ADMIN"))
                    ? "/fxml/admin-dashboard.fxml"
                    : "/fxml/student-dashboard.fxml";

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(dashboardFxml));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1200, 800);

            Stage stage = (Stage) rollNumberField.getScene().getWindow();
            stage. setScene(scene);
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
            Stage stage = (Stage) rollNumberField.getScene().getWindow();
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
        Stage stage = (Stage) rollNumberField.getScene().getWindow();
        Toast.show(stage, message, Toast.Type.SUCCESS);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void clearFields() {
        rollNumberField. clear();
        classroomPasswordField.clear();
    }
}