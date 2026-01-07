package com.classbuddy.util;

import javafx.scene.Parent;

public final class ViewTransitions {
    private ViewTransitions() {
    }

    public static void fadeIn(Parent root) {
        if (root == null) return;

        // Disabled: users reported fade causing a jarring flash on navigation.
        root.setOpacity(1);
    }
}
