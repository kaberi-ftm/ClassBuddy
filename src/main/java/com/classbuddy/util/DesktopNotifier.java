package com.classbuddy.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public final class DesktopNotifier {
    private static TrayIcon trayIcon;

    private DesktopNotifier() {}

    public static void init() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray not supported; OS notifications disabled.");
            return;
        }
        if (trayIcon != null) return;
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = loadIcon();
            trayIcon = new TrayIcon(image, "ClassBuddy");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (Exception e) {
            System.err.println("Failed to initialize system tray: " + e.getMessage());
        }
    }

    private static Image loadIcon() throws IOException {
        String[] candidates = new String[]{
                "/img/classbuddy_logo.png",
                "/img/logo.png"
        };
        for (String path : candidates) {
            try (InputStream is = DesktopNotifier.class.getResourceAsStream(path)) {
                if (is != null) {
                    return ImageIO.read(is);
                }
            }
        }
        // Fallback: blank 16x16
        Image img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 16, 16);
        g.dispose();
        return img;
    }

    public static void show(String title, String message) {
        if (trayIcon == null) return;
        try {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } catch (Exception ignored) {
        }
    }
}
