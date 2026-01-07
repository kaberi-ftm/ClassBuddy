package com.classbuddy.controller;

import com.classbuddy.model.Notification;
import com.classbuddy.service.NotificationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;
import com.classbuddy.model. Classroom;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import com.classbuddy.util.ViewTransitions;
import java.io.IOException;
import java.util.List;

import static com.classbuddy.controller.LoginController.currentUser;

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
    // Fix the initialize method:
    @FXML
    public void initialize() {
        // Get logged-in student
        currentStudent = LoginController.getCurrentUser();

        if (currentStudent != null) {
            studentNameLabel.setText(currentStudent.getUsername());
            System.out.println("Student logged in: " + currentStudent.getUsername());

            // Load classrooms student has joined
            loadStudentClassrooms();

            // Only update badge if the element exists
            if (notificationBadge != null) {
                updateNotificationBadge();
            }
        } else {
            System.err.println("Error: No user logged in");
        }
    }
    
    @FXML
    public void goToDashboard() {
        loadStudentClassrooms();
    }

    // Fix the updateNotificationBadge method:
    private void updateNotificationBadge() {
        if (currentStudent == null || notificationBadge == null) return;

        int unreadCount = NotificationService.getUnreadNotifications(currentStudent.getId()).size();
        if (unreadCount > 0) {
            notificationBadge.setText(String.valueOf(unreadCount));
            notificationBadge.setVisible(true);
        } else {
            notificationBadge.setVisible(false);
        }
    }

    // Fix showNotifications method:
    @FXML
    public void showNotifications() {
        if (currentStudent == null) return;

        List<Notification> notifications = NotificationService.getUnreadNotifications(currentStudent.getId());

        for (Notification notification : notifications) {
            System.out.println(notification.getTitle() + ": " + notification.getMessage());
        }

        NotificationService.markAllAsRead(currentStudent.getId());
        updateNotificationBadge();
    }


    /**
     * Load all classrooms this student has joined
     */
    private void loadStudentClassrooms() {
        classroomsContainer.getChildren().clear();

        if (currentStudent == null) {
            Label errorLabel = new Label("Error:  User not logged in");
            errorLabel.setStyle("-fx-text-fill: -error;");
            classroomsContainer.getChildren().add(errorLabel);
            return;
        }

        // Fetch classrooms from database
        List<Classroom> classrooms = ClassroomService
                .getStudentClassrooms(currentStudent.getId());

        if (classrooms. isEmpty()) {
            Label emptyLabel = new Label(
                    "No classrooms joined yet. Click 'Join Classroom' to get started!"
            );
            emptyLabel.setStyle("-fx-text-fill: -text-light; -fx-font-size: 14;");
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
        card.setSpacing(20);
        card.getStyleClass().add("classroom-card");
        card.setPrefHeight(120);

        VBox infoBox = new VBox();
        infoBox.setSpacing(10);

        Label nameLabel = new Label(classroom.getName());
        nameLabel.getStyleClass().add("classroom-name");

        Label codeLabel = new Label("ID: " + classroom.getId());
        codeLabel.getStyleClass().add("classroom-code");

        Label detailsLabel = new Label(
                "Section " + classroom.getSection() + " · " + classroom.getDepartment()
        );
        detailsLabel.getStyleClass().add("classroom-meta");

        infoBox.getChildren().addAll(nameLabel, codeLabel, detailsLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);

        Button viewBtn = new Button("View");
        viewBtn.getStyleClass().add("btn-primary");
        viewBtn.setOnAction(e -> viewClassroom(classroom));

        Button leaveBtn = new Button("Leave");
        leaveBtn.getStyleClass().add("btn-danger");
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
            ViewTransitions.fadeIn(root);

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
                System.out.println("Left classroom: " + classroom.getName());
                loadStudentClassrooms();  // Reload list
            }

        } catch (java.sql.SQLException e) {
            System.err.println("Error leaving classroom: " + e.getMessage());
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
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToCalendar() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/student-calendar.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            System.err.println("Failed to load calendar view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/profile.fxml")
            );
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(currentStudent, "/fxml/student-dashboard.fxml");

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            System.err.println("Failed to load profile: " + e.getMessage());
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
            ViewTransitions.fadeIn(root);

            LoginController.setCurrentUser(null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // In AdminDashboardController.java or StudentDashboardController.java

    @FXML
    private Label notificationBadge;



}