package com.classbuddy.controller;

import com.classbuddy.model.User;
import com.classbuddy.model.Notification;
import com.classbuddy.service.NotificationService;
import com.classbuddy.util.ViewTransitions;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

/**
 * Notification Center Controller - Display all notifications for current user
 */
public class NotificationCenterController {

    @FXML private VBox notificationContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button markAllReadBtn;
    @FXML private Label totalCountLabel;
    @FXML private Label unreadCountLabel;

    private User currentUser;
    private List<Notification> allNotifications;

    @FXML
    public void initialize() {
        currentUser = LoginController.getCurrentUser();
        if (currentUser != null) {
            loadNotifications();
            setupTypeFilter();
        }
    }

    private void setupTypeFilter() {
        typeFilter.getItems().addAll("All Types", "Routine", "Exam", "Notice", "Lab Test", "CT Quiz");
        typeFilter.setValue("All Types");
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndRefresh());
    }

    private void loadNotifications() {
        try {
            // Load all unread + recent read notifications
            allNotifications = NotificationService.getNotificationsForUser(currentUser.getId(), 100);
            refreshDisplay();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading notifications: " + e.getMessage());
        }
    }

    private void refreshDisplay() {
        notificationContainer.getChildren().clear();

        if (allNotifications == null || allNotifications.isEmpty()) {
            Label emptyLabel = new Label("No notifications");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 14;");
            notificationContainer.getChildren().add(emptyLabel);
            totalCountLabel.setText("0");
            unreadCountLabel.setText("0");
            return;
        }

        long unreadCount = allNotifications.stream().filter(n -> !n.isRead()).count();
        totalCountLabel.setText(String.valueOf(allNotifications.size()));
        unreadCountLabel.setText(String.valueOf(unreadCount));

        for (Notification notif : allNotifications) {
            VBox card = createNotificationCard(notif);
            notificationContainer.getChildren().add(card);
        }
    }

    private VBox createNotificationCard(Notification notif) {
        VBox card = new VBox(8);
        card.setStyle(
            "-fx-border-color: " + (notif.isRead() ? "#ddd" : "#2196F3") + "; " +
            "-fx-border-width: 1; " +
            "-fx-padding: 12; " +
            "-fx-background-color: " + (notif.isRead() ? "#f5f5f5" : "#f0f7ff") + "; " +
            "-fx-cursor: hand"
        );

        // Header: Title + close button
        HBox headerBox = new HBox(10);
        Label titleLabel = new Label(notif.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Label typeLabel = new Label(notif.getType().toString());
        typeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-padding: 2 6;");
        typeLabel.setStyle(typeLabel.getStyle() + "-fx-border-color: #ccc; -fx-border-radius: 3;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button markReadBtn = new Button(notif.isRead() ? "✓ Read" : "Mark Read");
        markReadBtn.setStyle("-fx-font-size: 11; -fx-padding: 4 8;");
        markReadBtn.setOnAction(e -> {
            NotificationService.markNotificationAsRead(notif.getId());
            notif.setRead(true);
            loadNotifications();
        });

        headerBox.getChildren().addAll(titleLabel, typeLabel, spacer, markReadBtn);

        // Message
        Label messageLabel = new Label(notif.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #333;");

        // Timestamp
        Label timeLabel = new Label(formatTime(notif.getCreatedAt()));
        timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #999;");

        card.getChildren().addAll(headerBox, messageLabel, timeLabel);

        // Click to expand details (optional future feature)
        card.setOnMouseClicked(e -> {
            if (!notif.isRead()) {
                NotificationService.markNotificationAsRead(notif.getId());
                notif.setRead(true);
                refreshDisplay();
            }
        });

        return card;
    }

    private void filterAndRefresh() {
        String selectedType = typeFilter.getValue();
        String searchTerm = searchField.getText().toLowerCase();

        notificationContainer.getChildren().clear();

        List<Notification> filtered = allNotifications.stream()
            .filter(n -> selectedType.equals("All Types") || n.getType().toString().equals(selectedType))
            .filter(n -> searchTerm.isEmpty() || 
                    n.getTitle().toLowerCase().contains(searchTerm) ||
                    n.getMessage().toLowerCase().contains(searchTerm))
            .toList();

        if (filtered.isEmpty()) {
            Label emptyLabel = new Label("No notifications match filters");
            emptyLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 14;");
            notificationContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Notification notif : filtered) {
            VBox card = createNotificationCard(notif);
            notificationContainer.getChildren().add(card);
        }
    }

    @FXML
    private void handleMarkAllRead() {
        if (allNotifications == null) return;
        
        for (Notification notif : allNotifications) {
            if (!notif.isRead()) {
                NotificationService.markNotificationAsRead(notif.getId());
            }
        }
        loadNotifications();
    }

    @FXML
    private void handleClearAll() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear All Notifications");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will delete all notifications. This action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (allNotifications != null) {
                for (Notification notif : allNotifications) {
                    NotificationService.deleteNotification(notif.getId());
                }
            }
            loadNotifications();
        }
    }

    @FXML
    private void handleGoBack() {
        try {
            FXMLLoader fxmlLoader;
            User user = LoginController.getCurrentUser();
            if (user != null && user.getRole().name().equals("ADMIN")) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            } else {
                fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/student-dashboard.fxml"));
            }
            
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) notificationContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(dateTime, now);
        
        if (duration.toMinutes() < 1) return "Just now";
        if (duration.toMinutes() < 60) return duration.toMinutes() + " min ago";
        if (duration.toHours() < 24) return duration.toHours() + " hours ago";
        if (duration.toDays() < 7) return duration.toDays() + " days ago";
        
        return dateTime.toLocalDate().toString();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
