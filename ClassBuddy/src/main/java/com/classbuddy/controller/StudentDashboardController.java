package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene. control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene. layout.VBox;
import javafx.stage.Stage;
import com.classbuddy.model. Classroom;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import java.io.IOException;
import java.util.List;

/**
 * Student Dashboard - Shows classrooms student has joined
 */
public class StudentDashboardController {
    @FXML
    private Label studentNameLabel;
    @FXML
    private VBox classroomsContainer;

    private User currentStudent;

    /**
     * Initialize - called when FXML loads
     */
    @FXML
    public void initialize() {
        // Get logged-in student
        currentStudent = LoginController.getCurrentUser();

        if (currentStudent != null) {
            studentNameLabel.setText("👤 " + currentStudent.getUsername());
            System.out.println("Student logged in: " + currentStudent.getUsername());
        }

        // Load classrooms student has joined
        loadStudentClassrooms();
    }

    /**
     * Load all classrooms this student has joined
     */
    private void loadStudentClassrooms() {
        classroomsContainer.getChildren().clear();

        if (currentStudent == null) {
            Label errorLabel = new Label("Error:  User not logged in");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");
            classroomsContainer.getChildren().add(errorLabel);
            return;
        }

        // Fetch classrooms from database
        List<Classroom> classrooms = ClassroomService
                .getStudentClassrooms(currentStudent.getId());

        if (classrooms. isEmpty()) {
            Label emptyLabel = new Label(
                    "📚 No classrooms joined yet.  Click 'Join Classroom' to get started!"
            );
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14;");
            classroomsContainer.getChildren().add(emptyLabel);

        } else {
            // Create card for each classroom
            for (Classroom classroom : classrooms) {
                classroomsContainer.getChildren()
                        .add(createClassroomCard(classroom));
            }
        }
    }

    /**
     * Create classroom card UI
     */
    private HBox createClassroomCard(Classroom classroom) {
        HBox card = new HBox();
        card.setSpacing(15);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #3498db;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding:  15;"
        );
        card.setPrefHeight(100);

        // Left:  Classroom info
        VBox infoBox = new VBox();
        infoBox.setSpacing(8);

        Label nameLabel = new Label(classroom.getName());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label detailsLabel = new Label(
                "Section: " + classroom.getSection() + " | " +
                        "Dept: " + classroom.getDepartment()
        );
        detailsLabel.setStyle("-fx-font-size: 12;");

        infoBox.getChildren().addAll(nameLabel, detailsLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Right: Buttons
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(8);

        javafx.scene.control.Button viewBtn =
                new javafx.scene. control.Button("👁️ View");
        viewBtn.setStyle("-fx-padding: 8 15;");
        viewBtn.setOnAction(e -> viewClassroom(classroom));

        javafx.scene.control.Button leaveBtn =
                new javafx.scene.control.Button("🚪 Leave");
        leaveBtn.setStyle(
                "-fx-padding: 8 15;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #e74c3c;"
        );
        leaveBtn.setOnAction(e -> leaveClassroom(classroom));

        buttonsBox.getChildren().addAll(viewBtn, leaveBtn);
        card.getChildren().addAll(infoBox, buttonsBox);

        return card;
    }

    /**
     * View classroom details
     */
    private void viewClassroom(Classroom classroom) {
        System.out.println("Viewing classroom: " + classroom.getName());
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/classroom-detail.fxml")
            );
            Parent root = fxmlLoader.load();

            ClassroomDetailController controller =
                    fxmlLoader.getController();
            controller.setClassroom(classroom);
            controller.setUser(currentStudent);

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading classroom details");
        }
    }

    /**
     * Leave classroom
     */
    private void leaveClassroom(Classroom classroom) {
        // Remove student from classroom
        String sql = "DELETE FROM classroom_students WHERE classroom_id = ?  " +
                "AND student_id = ?";

        try (java.sql.Connection conn = com.classbuddy.util.DatabaseUtil.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, classroom. getId());
            pstmt. setInt(2, currentStudent. getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Left classroom: " + classroom.getName());
                loadStudentClassrooms();  // Reload list
            }

        } catch (java.sql.SQLException e) {
            System.err.println("❌ Error leaving classroom: " + e.getMessage());
        }
    }

    /**
     * Navigate to join classroom
     */
    @FXML
    public void goToJoinClassroom() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/join-classroom.fxml")
            );
            Parent root = fxmlLoader.load();

            JoinClassroomController controller =
                    fxmlLoader.getController();
            controller. setAdmin(currentStudent);

            Scene scene = new Scene(root, 900, 600);
            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage. show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logout
     */
    @FXML
    public void handleLogout() {
        System.out.println("Student logging out.. .");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 900, 600);

            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            LoginController.setCurrentUser(null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}