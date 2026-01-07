package com.classbuddy.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public final class Toast {
    public enum Type {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    private static final Duration FADE_IN = Duration.millis(140);
    private static final Duration VISIBLE = Duration.millis(2200);
    private static final Duration FADE_OUT = Duration.millis(180);

    private static final double OFFSET_X = 24;
    private static final double OFFSET_Y = 24;
    private static final double MAX_TEXT_WIDTH = 360;

    private Toast() {
    }

    public static void show(Stage owner, String message, Type type) {
        if (owner == null || message == null || message.isBlank()) return;

        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);

            HBox container = new HBox(10);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(12, 14, 12, 14));
            container.getStyleClass().addAll("toast", toastClass(type));

            FontIcon icon = new FontIcon(iconLiteral(type));

            Label text = new Label(message);
            text.getStyleClass().add("toast-text");
            text.setWrapText(true);
            text.setMaxWidth(MAX_TEXT_WIDTH);

            container.getChildren().addAll(icon, text);

            Region root = container;
            popup.getContent().add(root);
            popup.show(owner);

            if (owner.getScene() != null && popup.getScene() != null) {
                popup.getScene().getStylesheets().addAll(owner.getScene().getStylesheets());
            }

            position(owner, popup, root);

            root.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(FADE_IN, root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            PauseTransition pause = new PauseTransition(VISIBLE);

            FadeTransition fadeOut = new FadeTransition(FADE_OUT, root);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> popup.hide());

            new SequentialTransition(fadeIn, pause, fadeOut).play();
        });
    }

    private static void position(Stage owner, Popup popup, Region toastRoot) {
        // Ensure CSS is applied so we have correct sizing
        toastRoot.applyCss();
        toastRoot.layout();

        double toastW = toastRoot.prefWidth(-1);
        double x = owner.getX() + owner.getWidth() - toastW - OFFSET_X;
        double y = owner.getY() + OFFSET_Y;

        popup.setX(Math.max(owner.getX(), x));
        popup.setY(Math.max(owner.getY(), y));
    }

    private static String toastClass(Type type) {
        return switch (type) {
            case SUCCESS -> "toast-success";
            case WARNING -> "toast-warning";
            case ERROR -> "toast-error";
            case INFO -> "toast-info";
        };
    }

    private static String iconLiteral(Type type) {
        return switch (type) {
            case SUCCESS -> "fas-check-circle";
            case WARNING -> "fas-exclamation-triangle";
            case ERROR -> "fas-times-circle";
            case INFO -> "fas-info-circle";
        };
    }
}
