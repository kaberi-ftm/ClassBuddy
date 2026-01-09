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

    private static final double DEFAULT_WIDTH = 1366;
    private static final double DEFAULT_HEIGHT = 800;
    private static final double MIN_WIDTH = 1366;
    private static final double MIN_HEIGHT = 800;

    private static final double LOGIN_WIDTH = 1366;
    private static final double LOGIN_HEIGHT = 800;
    private static final double LOGIN_MIN_WIDTH = 1366;
    private static final double LOGIN_MIN_HEIGHT = 800;

    public static void applyDashboardScene(Stage stage, Parent root) {
        Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);
        stage.show();
    }

    public static void applyLoginScene(Stage stage, Parent root) {
        Scene scene = new Scene(root, LOGIN_WIDTH, LOGIN_HEIGHT);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(LOGIN_MIN_WIDTH);
        stage.setMinHeight(LOGIN_MIN_HEIGHT);
        stage.setWidth(LOGIN_WIDTH);
        stage.setHeight(LOGIN_HEIGHT);
        stage.show();
    }

    /**
     * Navigate to a dashboard view with consistent sizing
     */
    public static void navigateToDashboard(String fxmlPath, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
        Parent root = loader.load();

        applyDashboardScene(stage, root);
    }

    /**
     * Navigate to login view
     */
    public static void navigateToLogin(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        applyLoginScene(stage, root);
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
        stage.setWidth(width);
        stage.setHeight(height);
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
