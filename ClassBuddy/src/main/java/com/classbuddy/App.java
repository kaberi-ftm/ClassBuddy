package com.classbuddy;

import com.classbuddy.service.NotificationScheduler;
import com.classbuddy.util.DatabaseUtil;
import com.classbuddy.util.ViewTransitions;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Initializing database...");
        DatabaseUtil.initializeDatabase();
        System.out.println("Database ready.");

        // Start notification scheduler
        NotificationScheduler.start();
        System.out.println("Notification system active.");

        System.out.println("════════════════════════════════════════════\n");

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("ClassBuddy - Classroom Management System");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(850);
        stage.setMinHeight(600);
        stage.setWidth(900);
        stage.setHeight(600);
        stage.show();
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