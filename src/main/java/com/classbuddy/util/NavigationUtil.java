package com.classbuddy.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class for consistent navigation and window management
 */
public class NavigationUtil {

    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 800;
    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 700;

    private static final double LOGIN_WIDTH = 900;
    private static final double LOGIN_HEIGHT = 600;
    private static final double LOGIN_MIN_WIDTH = 800;
    private static final double LOGIN_MIN_HEIGHT = 500;

    /**
     * Navigate to a dashboard view with consistent sizing
     */
    public static void navigateToDashboard(String fxmlPath, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.show();
    }

    /**
     * Navigate to login view
     */
    public static void navigateToLogin(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, LOGIN_WIDTH, LOGIN_HEIGHT);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(LOGIN_MIN_WIDTH);
        stage.setMinHeight(LOGIN_MIN_HEIGHT);
        stage.show();
    }

    /**
     * Navigate to any view with custom controller setup
     */
    public static <T> T navigateWithController(String fxmlPath, Stage stage, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.show();

        return loader.getController();
    }

    /**
     * Navigate with default dashboard sizing
     */
    public static void navigate(String fxmlPath, Stage stage) throws IOException {
        navigateToDashboard(fxmlPath, stage);
    }
}
