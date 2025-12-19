package com.classbuddy;

import com.classbuddy.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("====== ClassBuddy Application Starting ======");

        // Initialize database on app startup
        System.out.println("Initializing database...");
        DatabaseUtil. initializeDatabase();

        // Test connection
        DatabaseUtil.testConnection();

        System.out.println("Database path: " + DatabaseUtil.getDatabasePath());
        System.out.println("============================================\n");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        stage.setTitle("ClassBuddy");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        launch();
    }
}