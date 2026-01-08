package com.classbuddy.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene. Parent;
import javafx.scene.Scene;
import javafx. stage.Stage;

import java.io.IOException;

public class SceneNavigator {

    /**
     * Navigate to a new scene with resizing enabled
     */
    public static void navigateTo(Node currentNode, String fxmlPath, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) currentNode.getScene().getWindow();
            Scene scene = new Scene(root, width, height);

            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1600);
            stage.setMinHeight(900);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating to: " + fxmlPath);
        }
    }

    /**
     * Navigate and get controller reference
     */
    public static <T> T navigateToWithController(Node currentNode, String fxmlPath,
                                                 int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator. class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) currentNode.getScene().getWindow();
            Scene scene = new Scene(root, width, height);

            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1600);
            stage.setMinHeight(900);
            stage.show();

            return loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating to: " + fxmlPath);
            return null;
        }
    }

    /**
     * Open scene in fullscreen mode
     */
    public static void navigateToFullscreen(Node currentNode, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader. load();

            Stage stage = (Stage) currentNode.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setResizable(true);
            stage.setFullScreen(true);
            stage. setFullScreenExitHint("Press ESC to exit fullscreen");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err. println("Error navigating to fullscreen: " + fxmlPath);
        }
    }
}