package com.classbuddy.controller;

import com.classbuddy.model.User;
import com.classbuddy.model.Notification;
import com.classbuddy.service.NotificationService;
import com.classbuddy.util.DateFormats;
import com.classbuddy.util.NavigationUtil;
import com.classbuddy.util.ViewTransitions;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
            setupSearch();
        }
    }

    private void setupSearch() {
        if (searchField == null) return;
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndRefresh());
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
            VBox emptyBox = new VBox(6);
            emptyBox.getStyleClass().add("empty-state");

            Label title = new Label("No notifications");
            title.getStyleClass().add("empty-state-title");
            Label msg = new Label("You're all caught up.");
            msg.getStyleClass().add("empty-state-message");

            emptyBox.getChildren().addAll(title, msg);
            notificationContainer.getChildren().add(emptyBox);
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
        card.getStyleClass().add("notification-card");
        if (!notif.isRead()) {
            card.getStyleClass().add("notification-card-unread");
        }

        // Header: Title + close button
        HBox headerBox = new HBox(10);
        Label titleLabel = new Label(notif.getTitle());
        titleLabel.getStyleClass().add("notification-title");

        Label typeLabel = new Label(notif.getType().toString());
        typeLabel.getStyleClass().addAll("badge", "badge-blue");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button markReadBtn = new Button(notif.isRead() ? "✓ Read" : "Mark Read");
        markReadBtn.getStyleClass().addAll("btn-secondary", "btn-mini");
        markReadBtn.setDisable(notif.isRead());
        markReadBtn.setOnAction(e -> {
            NotificationService.markNotificationAsRead(notif.getId());
            notif.setRead(true);
            loadNotifications();
        });

        headerBox.getChildren().addAll(titleLabel, typeLabel, spacer, markReadBtn);

        // Message
        Label messageLabel = new Label(notif.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("notification-message");

        // Timestamp
        Label timeLabel = new Label(DateFormats.relativeTime(notif.getCreatedAt()));
        timeLabel.getStyleClass().add("notification-time");

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
        if (allNotifications == null) return;

        String selectedType = typeFilter != null ? typeFilter.getValue() : "All Types";
        String searchText = searchField != null ? searchField.getText() : "";
        String searchTerm = searchText == null ? "" : searchText.toLowerCase();

        notificationContainer.getChildren().clear();

        List<Notification> filtered = allNotifications.stream()
            .filter(n -> selectedType.equals("All Types") || n.getType().toString().equals(selectedType))
            .filter(n -> searchTerm.isEmpty() || 
                    n.getTitle().toLowerCase().contains(searchTerm) ||
                    n.getMessage().toLowerCase().contains(searchTerm))
            .toList();

        if (filtered.isEmpty()) {
            VBox emptyBox = new VBox(6);
            emptyBox.getStyleClass().add("empty-state");

            Label title = new Label("Nothing found");
            title.getStyleClass().add("empty-state-title");
            Label msg = new Label("Try clearing the search or changing the type.");
            msg.getStyleClass().add("empty-state-message");

            emptyBox.getChildren().addAll(title, msg);
            notificationContainer.getChildren().add(emptyBox);
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
            Stage stage = (Stage) notificationContainer.getScene().getWindow();
            NavigationUtil.applyDashboardScene(stage, root);
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
