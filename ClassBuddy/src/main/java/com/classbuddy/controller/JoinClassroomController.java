package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx. scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control. PasswordField;
import javafx. scene.control.TextField;
import javafx.stage.Stage;
import com.classbuddy.model. Classroom;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import java.io.IOException;

public class JoinClassroomController {
    @FXML
    private TextField rollNumberField;
    @FXML
    private PasswordField classroomPasswordField;
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
            showError("❌ Classroom not found.  Invalid password.");
            return;
        }

        // Check if roll is allowed in this classroom
        if (!ClassroomService.isRollAllowedInClassroom(classroom. getId(), rollNumber)) {
            showError("❌ Your roll number is not registered for this classroom.");
            return;
        }

        // Check if admin already joined
        if (ClassroomService.isStudentInClassroom(classroom.getId(), admin.getId())) {
            showError("✅ You already joined this classroom!");
            return;
        }

        // Add admin to classroom
        boolean added = ClassroomService.addStudentToClassroom(classroom.getId(), admin.getId(), rollNumber);

        if (added) {
            showSuccess("✅ Successfully joined classroom:  " + classroom.getName());
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            Parent root = fxmlLoader. load();
            Scene scene = new Scene(root, 1000, 700);

            Stage stage = (Stage) rollNumberField.getScene().getWindow();
            stage. setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error navigating back.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill:  #e74c3c;");
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
        rollNumberField. clear();
        classroomPasswordField.clear();
    }
}