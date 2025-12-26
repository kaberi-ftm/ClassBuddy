package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx. scene.Scene;
import javafx.scene. control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com. classbuddy.model. Exam;
import com.classbuddy.model.Notice;
import com.classbuddy.model. Routine;
import com.classbuddy.model.User;
import com.classbuddy.service.ExamService;
import com.classbuddy.service.NoticeService;
import com.classbuddy.service.RoutineService;
import java.io.IOException;
import java.util.List;

/**
 * Classroom Detail Screen - Shows routine, exams, notices
 */
public class ClassroomDetailController {
    @FXML
    private Label classroomNameLabel;
    @FXML
    private TabPane classroomTabs;  // Tabs for different sections
    @FXML
    private VBox routineContainer;
    @FXML
    private VBox examsContainer;
    @FXML
    private VBox noticesContainer;

    private Classroom classroom;
    private User user;

    /**
     * Set classroom and user
     */
    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Initialize
     */
    @FXML
    public void initialize() {
        if (classroom != null) {
            classroomNameLabel.setText(classroom.getName());
            loadClassroomData();
        }
    }

    /**
     * Load routine, exams, notices
     */
    private void loadClassroomData() {
        loadRoutine();
        loadExams();
        loadNotices();
    }

    /**
     * Load and display routine
     */
    private void loadRoutine() {
        routineContainer.getChildren().clear();

        // Get routine for all days
        List<Routine> routines = RoutineService.getWeeklyRoutine(classroom.getId());

        if (routines.isEmpty()) {
            Label emptyLabel = new Label("No routine set");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d;");
            routineContainer.getChildren().add(emptyLabel);
        } else {
            // Group by day
            java.util.Map<String, java.util.List<Routine>> byDay =
                    new java. util.LinkedHashMap<>();

            for (Routine routine : routines) {
                byDay.computeIfAbsent(routine.getDay(),
                        k -> new java. util.ArrayList<>()).add(routine);
            }

            // Create section for each day
            for (String day : byDay.keySet()) {
                Label dayLabel = new Label("📅 " + day);
                dayLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
                routineContainer. getChildren().add(dayLabel);

                for (Routine routine : byDay. get(day)) {
                    Label routineLabel = new Label(
                            "Period " + routine.getPeriodNumber() + ": " +
                                    routine.getCourseName() + " (" +
                                    routine.getTimeStart() + "-" +
                                    routine.getTimeEnd() + ")"
                    );
                    routineLabel.setStyle(
                            "-fx-padding: 5 15;" +
                                    "-fx-font-size: 12;"
                    );
                    routineContainer.getChildren().add(routineLabel);
                }
            }
        }
    }

    /**
     * Load and display exams
     */
    private void loadExams() {
        examsContainer.getChildren().clear();

        List<Exam> exams = ExamService. getClassroomExams(classroom.getId());

        if (exams.isEmpty()) {
            Label emptyLabel = new Label("No exams scheduled");
            emptyLabel.setStyle("-fx-text-fill:  #7f8c8d;");
            examsContainer.getChildren().add(emptyLabel);
        } else {
            for (Exam exam : exams) {
                HBox examBox = createExamBox(exam);
                examsContainer.getChildren().add(examBox);
            }
        }
    }

    /**
     * Create exam display box
     */
    private HBox createExamBox(Exam exam) {
        HBox box = new HBox();
        box.setSpacing(10);
        box.setStyle(
                "-fx-border-color: #f39c12;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding:  10;" +
                        "-fx-border-radius: 5;"
        );

        Label examLabel = new Label(
                "📝 " + exam.getCourseName() + " (" + exam.getExamType() + ") - " +
                        exam. getExamDate() + " at " + exam.getExamTime() +
                        " in " + exam.getRoom()
        );
        examLabel.setStyle("-fx-font-size: 12;");

        HBox.setHgrow(examLabel, javafx.scene.layout.Priority.ALWAYS);
        box.getChildren().add(examLabel);

        return box;
    }

    /**
     * Load and display notices
     */
    private void loadNotices() {
        noticesContainer.getChildren().clear();

        List<Notice> notices = NoticeService.getClassroomNotices(classroom.getId());

        if (notices.isEmpty()) {
            Label emptyLabel = new Label("No notices");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d;");
            noticesContainer.getChildren().add(emptyLabel);
        } else {
            for (Notice notice : notices) {
                VBox noticeBox = createNoticeBox(notice);
                noticesContainer.getChildren().add(noticeBox);
            }
        }
    }

    /**
     * Create notice display box
     */
    private VBox createNoticeBox(Notice notice) {
        VBox box = new VBox();
        box.setSpacing(8);
        box.setStyle(
                "-fx-border-color: " + (notice.isPinned() ? "#e74c3c" : "#bdc3c7") + ";" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 10;" +
                        "-fx-border-radius: 5;"
        );

        Label titleLabel = new Label(
                (notice.isPinned() ? "📌 " : "") + notice.getTitle()
        );
        titleLabel.setStyle("-fx-font-size: 13; -fx-font-weight:  bold;");

        Label categoryLabel = new Label("[" + notice.getCategory() + "]");
        categoryLabel. setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        Label contentLabel = new Label(notice.getContent());
        contentLabel.setStyle("-fx-font-size:  12; -fx-wrap-text: true;");
        contentLabel.setWrapText(true);

        Label timeLabel = new Label(notice.getCreatedAt().toString());
        timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #95a5a6;");

        box.getChildren().addAll(titleLabel, categoryLabel, contentLabel, timeLabel);

        return box;
    }

    /**
     * Go back to dashboard
     */
    @FXML
    public void goBackToDashboard() {
        try {
            FXMLLoader fxmlLoader;
            if (user. getRole().name().equals("ADMIN")) {
                fxmlLoader = new FXMLLoader(
                        getClass().getResource("/fxml/admin-dashboard.fxml")
                );
            } else {
                fxmlLoader = new FXMLLoader(
                        getClass().getResource("/fxml/student-dashboard.fxml")
                );
            }

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1000, 700);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}