package com.classbuddy.service;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Background service to check and send notifications periodically
 */
public class NotificationScheduler {

    private static Timer timer;
    private static final long CHECK_INTERVAL = 5 * 60 * 1000; // Check every 5 minutes
    private static final long INITIAL_DELAY = 30 * 1000; // Wait 30 seconds before first check

    /**
     * Start the notification scheduler
     */
    public static void start() {
        if (timer != null) {
            return;  // Already running
        }

        timer = new Timer("NotificationScheduler", true);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Checking for notifications...");

                try {
                    // Check for routine notifications (1 hour before class)
                    NotificationService.checkRoutineNotifications();

                    // Check for exam notifications (24 hours before exam)
                    NotificationService.checkExamNotifications();

                } catch (Exception e) {
                    System.err.println("Error in notification scheduler: " + e.getMessage());
                }
            }
        }, INITIAL_DELAY, CHECK_INTERVAL);  // Wait 30 seconds, then every 5 minutes

        System.out.println("Notification scheduler started");
    }

    /**
     * Stop the notification scheduler
     */
    public static void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("Notification scheduler stopped");
        }
    }
}