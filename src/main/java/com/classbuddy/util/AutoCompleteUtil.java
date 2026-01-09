package com.classbuddy.util;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.function.Function;

public class AutoCompleteUtil {

    public static void bind(TextField field, Function<String, List<String>> provider) {
        if (field == null || provider == null) return;
        ContextMenu menu = new ContextMenu();
        menu.setAutoHide(true);

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().length() < 2) {
                menu.hide();
                return;
            }
            List<String> options = provider.apply(newVal);
            if (options == null || options.isEmpty()) {
                menu.hide();
                return;
            }
            menu.getItems().clear();
            int count = Math.min(options.size(), 10);
            for (int i = 0; i < count; i++) {
                String opt = options.get(i);
                MenuItem item = new MenuItem(opt);
                item.setOnAction(e -> {
                    field.setText(opt);
                    field.positionCaret(opt.length());
                });
                menu.getItems().add(item);
            }
            if (!menu.isShowing()) {
                Platform.runLater(() -> {
                    menu.show(field, javafx.geometry.Side.BOTTOM, 0, 0);
                });
            }
        });

        field.focusedProperty().addListener((obs, was, is) -> {
            if (!is) menu.hide();
        });
    }
}
