package com.classbuddy.controller;

import com.classbuddy.model.User;
import com.classbuddy.model.UserProfile;
import com.classbuddy.service.ProfileService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.classbuddy.util.ViewTransitions;

import java.io.IOException;

/**
 * ProfileController - Manages user profile view and editing
 */
public class ProfileController {

    @FXML
    private Label usernameLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label roleLabel;
    
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField departmentField;
    
    @FXML
    private TextField studentIdField;

    @FXML
    private TextField rollNumberField;

    @FXML
    private Label rollNumberLabel;
    
    @FXML
    private TextField designationField;
    
    @FXML
    private TextArea addressArea;
    
    @FXML
    private TextArea bioArea;
    
    @FXML
    private DatePicker dobPicker;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label statusLabel;

    private User currentUser;
    private UserProfile currentProfile;
    private String returnView;  // View to return to after cancel

    @FXML
    public void initialize() {
        // Will be set via setUser method
    }

    /**
     * Set current user and load profile
     */
    public void setUser(User user, String returnView) {
        this.currentUser = user;
        this.returnView = returnView;
        
        if (currentUser != null) {
            loadUserInfo();
            loadProfile();
        }
    }

    /**
     * Load basic user information
     */
    private void loadUserInfo() {
        usernameLabel.setText(currentUser.getUsername());
        emailLabel.setText(currentUser.getEmail());
        roleLabel.setText(currentUser.getRole().toString());
    }

    /**
     * Load user profile from database
     */
    private void loadProfile() {
        currentProfile = ProfileService.getProfile(currentUser.getId());

        if (currentProfile != null) {
            fullNameField.setText(currentProfile.getFullName());
            phoneField.setText(currentProfile.getPhoneNumber());
            departmentField.setText(currentProfile.getDepartment());
            studentIdField.setText(currentProfile.getStudentId());
            rollNumberField.setText(currentProfile.getRollNumber());
            designationField.setText(currentProfile.getDesignation());
            addressArea.setText(currentProfile.getAddress());
            bioArea.setText(currentProfile.getBio());

            if (currentProfile.getDateOfBirth() != null) {
                dobPicker.setValue(currentProfile.getDateOfBirth().toLocalDate());
            }
        } else {
            // Create new profile
            currentProfile = new UserProfile(currentUser.getId());
        }

        // Show/hide fields based on role
        updateFieldVisibility();
    }

    /**
     * Update field visibility based on user role
     */
    private void updateFieldVisibility() {
        boolean isStudent = currentUser.getRole().toString().equalsIgnoreCase("STUDENT");
        studentIdField.setVisible(isStudent);
        studentIdField.setManaged(isStudent);
        rollNumberField.setVisible(isStudent);
        rollNumberField.setManaged(isStudent);
        if (rollNumberLabel != null) {
            rollNumberLabel.setVisible(isStudent);
            rollNumberLabel.setManaged(isStudent);
        }
        designationField.setVisible(!isStudent);
        designationField.setManaged(!isStudent);
    }

    /**
     * Save profile
     */
    @FXML
    public void handleSave() {
        // Validate required fields
        if (fullNameField.getText() == null || fullNameField.getText().trim().isEmpty()) {
            showStatus("Please enter your full name", false);
            return;
        }

        boolean isStudent = currentUser.getRole().toString().equalsIgnoreCase("STUDENT");
        if (isStudent) {
            String roll = rollNumberField.getText() != null ? rollNumberField.getText().trim() : "";
            if (roll.isEmpty()) {
                showStatus("Please enter your roll number", false);
                return;
            }

            if (!ProfileService.isRollNumberAvailable(roll, currentUser.getId())) {
                showStatus("That roll number is already in use", false);
                return;
            }
        }

        // Update profile object
        currentProfile.setFullName(fullNameField.getText().trim());
        currentProfile.setPhoneNumber(phoneField.getText().trim());
        currentProfile.setDepartment(departmentField.getText().trim());
        currentProfile.setStudentId(studentIdField.getText().trim());
        currentProfile.setRollNumber(rollNumberField.getText().trim());
        currentProfile.setDesignation(designationField.getText().trim());
        currentProfile.setAddress(addressArea.getText().trim());
        currentProfile.setBio(bioArea.getText().trim());

        if (dobPicker.getValue() != null) {
            currentProfile.setDateOfBirth(dobPicker.getValue().atStartOfDay());
        }

        // Save to database
        boolean success = ProfileService.saveProfile(currentProfile);

        if (success) {
            showStatus("Profile saved successfully!", true);
        } else {
            showStatus("Error saving profile", false);
        }
    }

    /**
     * Cancel and return to previous view
     */
    @FXML
    public void handleCancel() {
        navigateBack();
    }

    /**
     * Navigate back to previous view
     */
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(returnView));
            Parent root = loader.load();

            // Set user for the controller
            Object controller = loader.getController();
            if (controller instanceof StudentDashboardController) {
                // Student dashboard will auto-load via initialize
            } else if (controller instanceof AdminDashboardController) {
                // Admin dashboard will auto-load via initialize
            }

            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show status message
     */
    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-success", "status-danger");
        statusLabel.getStyleClass().add(success ? "status-success" : "status-danger");
        statusLabel.setVisible(true);

        // Hide after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> statusLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
