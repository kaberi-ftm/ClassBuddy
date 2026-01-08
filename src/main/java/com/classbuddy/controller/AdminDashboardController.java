package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import com.classbuddy.util.NavigationUtil;
import com.classbuddy.util.ViewTransitions;
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
        currentAdmin = LoginController.getCurrentUser();

        if (currentAdmin != null) {
            adminNameLabel.setText(currentAdmin.getUsername());
            System.out.println("Admin logged in: " + currentAdmin.getUsername());
        }

        refreshClassrooms();
    }
    
    @FXML
    public void goToDashboard() {
        refreshClassrooms();
    }

    @FXML
    public void refreshClassrooms() {
        classroomsContainer.getChildren().clear();

        if (currentAdmin == null) {
            Label errorLabel = new Label("Error: User not logged in");
            errorLabel.getStyleClass().addAll("text-body-md", "text-error");
            classroomsContainer.getChildren().add(errorLabel);
            return;
        }

        List<Classroom> classrooms = ClassroomService.getAdminClassrooms(currentAdmin.getId());

        if (classrooms.isEmpty()) {
            Label emptyLabel = new Label("No classrooms yet. Create one to get started!");
            emptyLabel.getStyleClass().addAll("text-body-md", "text-tertiary");
            classroomsContainer.getChildren().add(emptyLabel);
        } else {
            for (Classroom classroom : classrooms) {
                classroomsContainer.getChildren().add(createClassroomCard(classroom));
            }
        }
    }

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
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button openBtn = new Button("Open");
        openBtn.getStyleClass().addAll("btn", "btn-primary");
        openBtn.setMinWidth(96);
        openBtn.setOnAction(e -> openClassroom(classroom));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().addAll("btn", "btn-secondary");
        editBtn.setMinWidth(96);
        editBtn.setOnAction(e -> editClassroom(classroom));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setMinWidth(96);
        deleteBtn.setOnAction(e -> deleteClassroom(classroom));

        buttonsBox.getChildren().addAll(openBtn, editBtn, deleteBtn);

        card.getChildren().addAll(infoBox, buttonsBox);
        return card;
    }

    // FIXED: Open classroom details
    private void openClassroom(Classroom classroom) {
        System.out.println("Opening classroom: " + classroom.getName());
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/classroom-detail.fxml")
            );
            Parent root = fxmlLoader.load();

            ClassroomDetailController controller = fxmlLoader.getController();
            controller.setClassroom(classroom);
            controller.setUser(currentAdmin);
            controller.loadData(); // Call this after setting data
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading classroom details: " + e.getMessage());
        }
    }

    // FIXED: Edit classroom - navigate to manage students
    private void editClassroom(Classroom classroom) {
        System.out.println("Editing classroom: " + classroom.getName());
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/fxml/manage-students.fxml")
            );
            Parent root = fxmlLoader.load();

            ManageStudentsController controller = fxmlLoader.getController();
            controller.setClassroom(classroom);
            controller.loadData(); // Call after setting classroom
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading manage students page: " + e.getMessage());
        }
    }

    private void deleteClassroom(Classroom classroom) {
        System.out.println("Deleting classroom: " + classroom.getName());
        if (ClassroomService.deleteClassroom(classroom.getId())) {
            System.out.println("Classroom deleted successfully");
            refreshClassrooms();
        }
    }

    @FXML
    public void goToCreateClassroom() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/create-classroom.fxml"));
            Parent root = fxmlLoader.load();

            CreateClassroomController controller = fxmlLoader.getController();
            controller.setAdmin(currentAdmin);
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading create classroom page:  " + e.getMessage());
        }
    }

    @FXML
    public void goToJoinClassroom() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/join-classroom.fxml"));
            Parent root = fxmlLoader.load();

            JoinClassroomController controller = fxmlLoader.getController();
            controller.setAdmin(currentAdmin);
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading join classroom page: " + e.getMessage());
        }
    }

    @FXML
    public void goToCalendar() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/admin-calendar.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
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
            controller.setUser(currentAdmin, "/fxml/admin-dashboard.fxml");
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            System.err.println("Failed to load profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void goToExportImport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/schedule-export-import.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading export/import page: " + e.getMessage());
        }
    }

    @FXML
    public void goToExamMarks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exam-marks.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading exam marks page: " + e.getMessage());
        }
    }

    @FXML
    public void goToAnalytics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-analytics.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading analytics page: " + e.getMessage());
        }
    }

    @FXML
    public void goToNotificationCenter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/notification-center.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading notification center: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        System.out.println("Admin logging out...");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            NavigationUtil.applyLoginScene(stage, root);
            ViewTransitions.fadeIn(root);

            LoginController.setCurrentUser(null);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error logging out: " + e.getMessage());
        }
    }
}