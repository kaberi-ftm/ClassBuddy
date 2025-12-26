package com.classbuddy.controller;
import javafx.fxml. FXML;
import javafx. fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene. Scene;
import javafx.scene. control. Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene. layout.VBox;
import javafx.stage.Stage;
import com.classbuddy.model. Classroom;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import java.io.IOException;
import java.util.List;

public class AdminDashboardController {
    @FXML
    private Label adminNameLabel;
    @FXML
    private VBox classroomsContainer;

    private User currentAdmin;

    @FXML
    public void initialize() {
        // Get current logged-in admin
        currentAdmin = LoginController.getCurrentUser();

        if (currentAdmin != null) {
            adminNameLabel.setText("👤 " + currentAdmin.getUsername());
            System.out.println("Admin logged in: " + currentAdmin.getUsername());
        }


        refreshClassrooms();
    }

    @FXML
    public void refreshClassrooms() {
        classroomsContainer.getChildren().clear();

        if (currentAdmin == null) {
            Label errorLabel = new Label("Error: User not logged in");
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14;");
            classroomsContainer.getChildren().add(errorLabel);
            return;
        }

        // Fetch classrooms from database
        List<Classroom> classrooms = ClassroomService.getAdminClassrooms(currentAdmin. getId());

        if (classrooms. isEmpty()) {
            Label emptyLabel = new Label("📚 No classrooms yet. Create one to get started!");
            emptyLabel.setStyle("-fx-text-fill: blue; -fx-font-size: 14;");
            classroomsContainer.getChildren().add(emptyLabel);
        } else {
            for (Classroom classroom : classrooms) {
                classroomsContainer.getChildren().add(createClassroomCard(classroom));
            }
        }
    }


    /**
     * Create a classroom card UI component
     */
    private HBox createClassroomCard(Classroom classroom) {
        HBox card = new HBox();
        card.setSpacing(15);
        card.setStyle(
                "-fx-background-color:  white;" +
                        "-fx-border-color: blue;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.2, 0, 1);"
        );
        card.setPrefHeight(100);


        VBox infoBox = new VBox();
        infoBox.setSpacing(8);

        Label nameLabel = new Label(classroom.getName());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label detailsLabel = new Label(
                "Section: " + classroom.getSection() + " | " +
                        "Dept: " + classroom.getDepartment()
        );
        detailsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        infoBox.getChildren().addAll(nameLabel, detailsLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Action buttons (right side)
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(8);

        Button openBtn = new Button("📖 Open");
        openBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 15;");
        openBtn.setOnAction(e -> openClassroom(classroom));

        Button editBtn = new Button("✏️ Edit");
        editBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 15;");
        editBtn.setOnAction(e -> editClassroom(classroom));

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setStyle("-fx-font-size: 12; -fx-padding: 8 15; -fx-text-fill: white; -fx-background-color: #e74c3c;");
        deleteBtn.setOnAction(e -> deleteClassroom(classroom));

        buttonsBox.getChildren().addAll(openBtn, editBtn, deleteBtn);

        card.getChildren().addAll(infoBox, buttonsBox);
        return card;
    }
    private void openClassroom(Classroom classroom) {
        System.out.println("Opening classroom: " + classroom.getName());
        // TODO: Navigate to classroom management screen
    }

    private void editClassroom(Classroom classroom) {
        System.out.println("Editing classroom: " + classroom. getName());
        // TODO: Open edit classroom dialog
    }

    private void deleteClassroom(Classroom classroom) {
        System.out.println("Deleting classroom: " + classroom.getName());
        // TODO: Add confirmation dialog and delete
        if (ClassroomService.deleteClassroom(classroom.getId())) {
            System.out.println("Classroom deleted successfully");
            refreshClassrooms();
        }
    }

    @FXML
    public void goToCreateClassroom() {
        System.out.println("Navigating to create classroom.. .");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/create-classroom.fxml"));
            Parent root = fxmlLoader.load();

            // Pass current admin to the controller
            CreateClassroomController controller = fxmlLoader.getController();
            controller.setAdmin(currentAdmin);

            Scene scene = new Scene(root, 900, 600);
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading create classroom page: " + e.getMessage());
        }
    }

    @FXML
    public void goToJoinClassroom() {
        System.out.println("Navigating to join classroom...");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/join-classroom.fxml"));
            Parent root = fxmlLoader.load();

            JoinClassroomController controller = fxmlLoader.getController();
            controller.setAdmin(currentAdmin);

            Scene scene = new Scene(root, 900, 600);
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage. show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading join classroom page: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        System.out.println("Admin logging out...");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 900, 600);

            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            // Clear current user
            LoginController.setCurrentUser(null);
        } catch (IOException e) {
            e.printStackTrace();
            System. err.println("Error logging out: " + e.getMessage());
        }
    }
}