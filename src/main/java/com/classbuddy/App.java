package com.classbuddy;

import com.classbuddy.service.NotificationScheduler;
import com.classbuddy.util.DatabaseUtil;
import com.classbuddy.util.NavigationUtil;
import com.classbuddy.util.ViewTransitions;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Initializing database...");
        DatabaseUtil.initializeDatabase();
        System.out.println("Database ready.");

        // Initialize system notifications and start scheduler
        com.classbuddy.util.DesktopNotifier.init();
        NotificationScheduler.start();
        System.out.println("Notification system active.");

        System.out.println("════════════════════════════════════════════\n");

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = fxmlLoader.load();

        stage.setTitle("ClassBuddy - Classroom Management System");
        NavigationUtil.applyLoginScene(stage, root);
        ViewTransitions.fadeIn(root);
        // Stop scheduler when app closes
        stage.setOnCloseRequest(e -> {
            NotificationScheduler.stop();
            System.out.println("ClassBuddy closed");
        });
    }

    public static void main(String[] args) {
        launch();
    }
}