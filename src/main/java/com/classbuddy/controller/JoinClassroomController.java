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
import javafx.stage.FileChooser;
import javafx.scene.layout.VBox;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.classbuddy.model. Classroom;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import com.classbuddy.service.ProfileService;
import com.classbuddy.model.UserProfile;
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
    private TextField classIdField;

    @FXML
    private TextField rollNumberField;
    @FXML
    private PasswordField classroomPasswordField;
    @FXML
    private Label errorLabel;

    // Wizard UI
    @FXML private VBox step1Box;
    @FXML private VBox step2Box;
    @FXML private VBox step3Box;
    @FXML private Label progressLabel;
    @FXML private Label classroomPreviewLabel;

    private User currentUser;
    private int currentStep = 1;
    private Classroom selectedClassroom;

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

        updateStepUI();
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

            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) rollNumberField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1366);
            stage.setMinHeight(800);
            stage.setWidth(1366);
            stage.setHeight(800);
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

            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) rollNumberField.getScene().getWindow();
            stage.setScene(scene);
            stage.setMinWidth(1366);
            stage.setMinHeight(800);
            stage.setWidth(1366);
            stage.setHeight(800);
            stage.show();
            ViewTransitions.fadeIn(root);

            LoginController.setCurrentUser(null);
        } catch (IOException e) {
            showError("Logout failed.");
        }
    }

    @FXML
    public void handleJoinClassroom() {
        String classId = classIdField.getText() != null ? classIdField.getText().trim() : "";
        String rollNumber = rollNumberField.getText().trim();
        String password = classroomPasswordField.getText();

        if (classId.isEmpty() || rollNumber.isEmpty() || password.isEmpty()) {
            showError("Please enter Class ID, roll number and classroom password.");
            return;
        }

        Classroom classroom = ClassroomService.getClassroomByClassId(classId);

        if (classroom == null) {
            showError("Classroom not found. Check the Class ID.");
            return;
        }

        if (!com.classbuddy.util.PasswordHasher.verifyPassword(password, classroom.getPasswordHash())) {
            showError("Invalid classroom password.");
            return;
        }

        User u = currentUser != null ? currentUser : LoginController.getCurrentUser();
        if (u == null) {
            showError("User session not found. Please login again.");
            return;
        }

        UserProfile profile = ProfileService.getProfile(u.getId());
        if (profile == null || profile.getRollNumber() == null || profile.getRollNumber().trim().isEmpty()) {
            showError("Set your roll number in Profile first.");
            return;
        }

        if (!profile.getRollNumber().trim().equalsIgnoreCase(rollNumber)) {
            showError("Roll number doesn't match your profile.");
            return;
        }

        // Check if roll is allowed in this classroom
        if (!ClassroomService.isRollAllowedInClassroom(classroom. getId(), rollNumber)) {
            showError("Your roll number is not registered for this classroom.");
            return;
        }

        // Check if admin already joined
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

    // Step wizard: UI management
    private void updateStepUI() {
        boolean s1 = currentStep == 1;
        boolean s2 = currentStep == 2;
        boolean s3 = currentStep == 3;

        if (step1Box != null) { step1Box.setVisible(s1); step1Box.setManaged(s1); }
        if (step2Box != null) { step2Box.setVisible(s2); step2Box.setManaged(s2); }
        if (step3Box != null) { step3Box.setVisible(s3); step3Box.setManaged(s3); }
        if (progressLabel != null) { progressLabel.setText("Step " + currentStep + " of 3"); }

        if (errorLabel != null) { errorLabel.setVisible(false); errorLabel.setManaged(false); errorLabel.setText(""); }
    }

    @FXML
    private void handleScanQr() {
        try {
            Stage stage = (Stage) (classIdField != null ? classIdField.getScene().getWindow() : null);
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select QR Image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = chooser.showOpenDialog(stage);
            if (file == null) { return; }

            BufferedImage image = ImageIO.read(file);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            Result result = new MultiFormatReader().decode(bitmap);
            String qrText = result.getText();

            if (qrText != null && !qrText.trim().isEmpty()) {
                classIdField.setText(qrText.trim());
                validateClassIdAndProceed();
            } else {
                showError("QR code did not contain a valid Class ID.");
            }
        } catch (Exception ex) {
            showError("Failed to read QR code image.");
        }
    }

    @FXML
    private void nextFromStep2() {
        if (selectedClassroom == null) {
            showError("No classroom selected. Go back and validate Class ID.");
            return;
        }
        String password = classroomPasswordField.getText();
        if (password == null || password.trim().isEmpty()) {
            showError("Please enter the classroom password.");
            return;
        }

        boolean ok = ClassroomService.verifyClassroomPassword(selectedClassroom.getId(), password);
        if (!ok) {
            showError("Invalid classroom password.");
            return;
        }

        currentStep = 3;
        updateStepUI();
    }

    @FXML
    private void backToStep1() {
        currentStep = 1;
        updateStepUI();
    }

    @FXML
    private void backToStep2() {
        currentStep = 2;
        updateStepUI();
    }

    @FXML
    private void joinFromStep3() {
        if (selectedClassroom == null) {
            showError("No classroom selected. Go back and validate Class ID.");
            return;
        }

        String rollNumber = rollNumberField.getText() != null ? rollNumberField.getText().trim() : "";
        if (rollNumber.isEmpty()) {
            showError("Please enter your roll number.");
            return;
        }

        User u = currentUser != null ? currentUser : LoginController.getCurrentUser();
        if (u == null) {
            showError("User session not found. Please login again.");
            return;
        }

        UserProfile profile = ProfileService.getProfile(u.getId());
        if (profile == null || profile.getRollNumber() == null || profile.getRollNumber().trim().isEmpty()) {
            showError("Set your roll number in Profile first.");
            return;
        }

        if (!profile.getRollNumber().trim().equalsIgnoreCase(rollNumber)) {
            showError("Roll number doesn't match your profile.");
            return;
        }

        if (!ClassroomService.isRollAllowedInClassroom(selectedClassroom.getId(), rollNumber)) {
            showError("Your roll number is not registered for this classroom.");
            return;
        }

        if (ClassroomService.isStudentInClassroom(selectedClassroom.getId(), u.getId())) {
            showError("You already joined this classroom.");
            return;
        }

        boolean added = ClassroomService.addStudentToClassroom(selectedClassroom.getId(), u.getId(), rollNumber);
        if (added) {
            showSuccess("Joined classroom: " + selectedClassroom.getName());
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
            showError("Error joining classroom. Please try again.");
        }
    }

    private boolean validateClassIdAndProceed() {
        String classId = classIdField.getText() != null ? classIdField.getText().trim() : "";
        if (classId.isEmpty()) {
            showError("Please enter a valid Class ID.");
            return false;
        }

        Classroom classroom = ClassroomService.getClassroomByClassId(classId);
        if (classroom == null) {
            showError("Classroom not found. Check the Class ID.");
            return false;
        }

        selectedClassroom = classroom;
        if (classroomPreviewLabel != null) {
            String preview = String.format("%s | Section: %s | Dept: %s | Class ID: %s",
                    classroom.getName(), classroom.getSection(), classroom.getDepartment(), classId);
            classroomPreviewLabel.setText(preview);
        }

        currentStep = 2;
        updateStepUI();
        return true;
    }

    @FXML
    private void nextFromStep1() {
        validateClassIdAndProceed();
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
            Scene scene = new Scene(root, 1366, 800);

            Stage stage = (Stage) rollNumberField.getScene().getWindow();
            stage. setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1366);
            stage.setMinHeight(800);
            stage.setWidth(1366);
            stage.setHeight(800);
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

            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) rollNumberField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1366);
            stage.setMinHeight(800);
            stage.setWidth(1366);
            stage.setHeight(800);
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