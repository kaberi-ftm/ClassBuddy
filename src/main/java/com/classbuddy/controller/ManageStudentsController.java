package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import com.classbuddy.util.ViewTransitions;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com.classbuddy.service.ClassroomService;
import java.io.IOException;
import java.sql.*;

/**
 * Admin Screen - Add/Remove students from classroom
 */
public class ManageStudentsController {
    @FXML
    private Label classroomNameLabel;
    @FXML
    private TextField rollNumberField;
    @FXML
    private TextField studentNameField;
    @FXML
    private VBox studentsListContainer;
    @FXML
    private Label messageLabel;

    private Classroom classroom;

    /**
     * Set classroom
     */
    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    /**
     * Initialize - DON'T load data here
     */
    @FXML
    public void initialize() {
        // Don't load data here - classroom is null
    }

    /**
     * IMPORTANT: Call this AFTER setting classroom
     */
    public void loadData() {
        if (classroom != null) {
            classroomNameLabel.setText(classroom.getName());
            loadStudents();
        } else {
            System.err.println("ERROR: Classroom is null in loadData()");
        }
    }

    /**
     * Add roll number (allow student to join)
     */
    @FXML
    public void handleAddRoll() {
        String rollNumber = rollNumberField.getText().trim();
        String studentName = studentNameField.getText().trim();

        if (rollNumber.isEmpty()) {
            showError("Please enter roll number");
            return;
        }

        boolean added = ClassroomService.addRollToClassroom(
                classroom.getId(), rollNumber, studentName
        );

        if (added) {
            showSuccess("Roll number added successfully");
            rollNumberField.clear();
            studentNameField.clear();
            loadStudents();
        } else {
            showError("Roll already exists or an error occurred");
        }
    }

    /**
     * Load all students allowed in this classroom
     */
    private void loadStudents() {
        studentsListContainer.getChildren().clear();

        String sql = "SELECT * FROM classroom_rolls WHERE classroom_id = ?";

        try (Connection conn = com.classbuddy.util.DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroom.getId());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    HBox studentBox = createStudentBox(
                            rs.getInt("id"),
                            rs.getString("roll_number"),
                            rs.getString("student_name")
                    );
                    studentsListContainer.getChildren().add(studentBox);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading students");
        }
    }

    /**
     * Create student display box
     */
    private HBox createStudentBox(int rollId, String rollNumber, String studentName) {
        HBox box = new HBox();
        box.setSpacing(10);
        box.setStyle(
                "-fx-background-color: -card-bg;" +
                        "-fx-border-color: -border-color;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 12;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        Label infoLabel = new Label(
                rollNumber + (studentName != null && !studentName.isEmpty()
                        ? " - " + studentName : "")
        );

        Button deleteBtn = new Button("Remove");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(e -> removeRoll(rollId));

        HBox.setHgrow(infoLabel, javafx.scene.layout.Priority.ALWAYS);
        box.getChildren().addAll(infoLabel, deleteBtn);

        return box;
    }

    /**
     * Remove roll from classroom
     */
    private void removeRoll(int rollId) {
        String sql = "DELETE FROM classroom_rolls WHERE id = ?";

        try (Connection conn = com.classbuddy.util.DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rollId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showSuccess("Roll removed successfully");
                loadStudents();
            }

        } catch (SQLException e) {
            showError("Error removing roll");
        }
    }

    /**
     * Go back
     */
    @FXML
    public void goBack() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/admin-dashboard.fxml")
            );
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
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

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: -error;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: -success;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}