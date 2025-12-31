package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx. scene.Scene;
import javafx.scene. control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com.classbuddy.model.Exam;
import com.classbuddy.model.Notice;
import com.classbuddy.model.Routine;
import com.classbuddy.model.User;
import com.classbuddy.model.CTQuiz;
import com.classbuddy.model.LabTest;
import com.classbuddy.service.ExamService;
import com.classbuddy.service.NoticeService;
import com.classbuddy.service.RoutineService;
import com.classbuddy.service.CTQuizService;
import com.classbuddy.service.LabTestService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    @FXML
    private VBox ctQuizContainer;
    @FXML
    private VBox labTestContainer;

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
        if (classroom != null && user != null) {
            classroomNameLabel.setText(classroom.getName());
            loadClassroomData();
            
            // Add admin controls if user is admin
            if (user.getRole().name().equals("ADMIN")) {
                addAdminControls();
            }
        }
    }

    /**
     * Add admin control buttons to the UI
     */
    private void addAdminControls() {
        // This method can be extended to add floating action buttons
        // or admin-specific toolbar buttons to each tab
        System.out.println("Admin controls available");
    }

    /**
     * Load routine, exams, notices, CT/Quiz, Lab Tests
     */
    private void loadClassroomData() {
        loadRoutine();
        loadExams();
        loadNotices();
        loadCTQuizzes();
        loadLabTests();
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
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14;");
            routineContainer.getChildren().add(emptyLabel);
        } else {
            // Create a calendar-style weekly routine view
            VBox calendarView = createCalendarStyleRoutine(routines);
            routineContainer.getChildren().add(calendarView);
        }
    }

    /**
     * Create a calendar-style routine view
     */
    private VBox createCalendarStyleRoutine(List<Routine> routines) {
        VBox calendarBox = new VBox(15);
        calendarBox.setStyle("-fx-padding: 10;");

        // Group by day
        java.util.Map<String, java.util.List<Routine>> byDay =
                new java.util.LinkedHashMap<>();

        String[] daysOrder = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : daysOrder) {
            byDay.put(day, new java.util.ArrayList<>());
        }

        for (Routine routine : routines) {
            byDay.computeIfAbsent(routine.getDay(), k -> new java.util.ArrayList<>()).add(routine);
        }

        // Create section for each day
        for (String day : daysOrder) {
            List<Routine> dayRoutines = byDay.get(day);
            if (dayRoutines == null || dayRoutines.isEmpty()) continue;

            // Day header with gradient background
            Label dayLabel = new Label("📅 " + day);
            dayLabel.setStyle(
                    "-fx-font-size: 16; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
                    "-fx-padding: 10 15; " +
                    "-fx-background-radius: 8 8 0 0;"
            );

            // Periods container
            VBox periodsBox = new VBox(8);
            periodsBox.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #dee5ed; " +
                    "-fx-border-width: 0 1 1 1; " +
                    "-fx-border-radius: 0 0 8 8; " +
                    "-fx-background-radius: 0 0 8 8; " +
                    "-fx-padding: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 8, 0.2, 0, 2);"
            );

            // Sort by period number
            dayRoutines.sort((r1, r2) -> Integer.compare(r1.getPeriodNumber(), r2.getPeriodNumber()));

            // Create card for each period with color coding
            String[] colors = {"#ff6b6b", "#4ecdc4", "#45b7d1", "#f9ca24", "#6c5ce7", "#a29bfe"};
            int colorIndex = 0;

            for (Routine routine : dayRoutines) {
                HBox periodCard = new HBox(15);
                periodCard.setStyle(
                        "-fx-background-color: " + colors[colorIndex % colors.length] + "20; " +
                        "-fx-border-color: " + colors[colorIndex % colors.length] + "; " +
                        "-fx-border-width: 0 0 0 4; " +
                        "-fx-padding: 12; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;"
                );

                VBox periodInfo = new VBox(5);

                Label periodLabel = new Label("Period " + routine.getPeriodNumber());
                periodLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #666;");

                Label courseLabel = new Label(routine.getCourseName());
                courseLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                Label detailsLabel = new Label(
                        "🕐 " + routine.getTimeStart() + " - " + routine.getTimeEnd() + " | " +
                        "👨‍🏫 " + (routine.getTeacherName() != null ? routine.getTeacherName() : "TBA") + " | " +
                        "🚪 Room " + (routine.getRoom() != null ? routine.getRoom() : "TBA")
                );
                detailsLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");

                periodInfo.getChildren().addAll(periodLabel, courseLabel, detailsLabel);
                periodCard.getChildren().add(periodInfo);

                periodsBox.getChildren().add(periodCard);
                colorIndex++;
            }

            VBox dayContainer = new VBox();
            dayContainer.getChildren().addAll(dayLabel, periodsBox);

            calendarBox.getChildren().add(dayContainer);
        }

        return calendarBox;
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
     * Load and display CT/Quiz entries
     */
    private void loadCTQuizzes() {
        if (ctQuizContainer == null) return;
        
        ctQuizContainer.getChildren().clear();

        List<CTQuiz> ctQuizzes = CTQuizService.getClassroomCTQuizzes(classroom.getId());

        if (ctQuizzes.isEmpty()) {
            Label emptyLabel = new Label("No CT/Quiz scheduled");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d;");
            ctQuizContainer.getChildren().add(emptyLabel);
        } else {
            for (CTQuiz ctQuiz : ctQuizzes) {
                HBox ctBox = createCTQuizBox(ctQuiz);
                ctQuizContainer.getChildren().add(ctBox);
            }
        }
    }

    /**
     * Create CT/Quiz display box
     */
    private HBox createCTQuizBox(CTQuiz ctQuiz) {
        HBox box = new HBox();
        box.setSpacing(10);
        
        // Check if urgent (deadline within 3 days)
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), ctQuiz.getDeadline());
        boolean isUrgent = daysUntil >= 0 && daysUntil <= 3;
        
        String borderColor = ctQuiz.isCompleted() ? "#27ae60" : (isUrgent ? "#e74c3c" : "#3498db");
        
        box.setStyle(
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-width: 2;" +
                "-fx-padding: 10;" +
                "-fx-border-radius: 5;"
        );

        VBox contentBox = new VBox(5);
        
        Label nameLabel = new Label(
                "📝 " + ctQuiz.getName() + 
                (ctQuiz.isCompleted() ? " ✅" : (isUrgent ? " ⚠️ URGENT" : ""))
        );
        nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        Label deadlineLabel = new Label("Deadline: " + ctQuiz.getDeadline());
        deadlineLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

        if (ctQuiz.getSyllabus() != null && !ctQuiz.getSyllabus().isEmpty()) {
            Label syllabusLabel = new Label("Syllabus: " + ctQuiz.getSyllabus());
            syllabusLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #777;");
            syllabusLabel.setWrapText(true);
            contentBox.getChildren().addAll(nameLabel, deadlineLabel, syllabusLabel);
        } else {
            contentBox.getChildren().addAll(nameLabel, deadlineLabel);
        }

        HBox.setHgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);
        box.getChildren().add(contentBox);

        return box;
    }

    /**
     * Load and display lab tests
     */
    private void loadLabTests() {
        if (labTestContainer == null) return;
        
        labTestContainer.getChildren().clear();

        List<LabTest> labTests = LabTestService.getClassroomLabTests(classroom.getId());

        if (labTests.isEmpty()) {
            Label emptyLabel = new Label("No lab tests scheduled");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d;");
            labTestContainer.getChildren().add(emptyLabel);
        } else {
            for (LabTest labTest : labTests) {
                HBox labBox = createLabTestBox(labTest);
                labTestContainer.getChildren().add(labBox);
            }
        }
    }

    /**
     * Create lab test display box
     */
    private HBox createLabTestBox(LabTest labTest) {
        HBox box = new HBox();
        box.setSpacing(10);
        box.setStyle(
                "-fx-border-color: #9b59b6;" +
                "-fx-border-width: 2;" +
                "-fx-padding: 10;" +
                "-fx-border-radius: 5;"
        );

        VBox contentBox = new VBox(5);
        
        Label titleLabel = new Label("🔬 Lab Test - Experiment " + labTest.getExperimentNumber());
        titleLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        Label dateLabel = new Label("Date: " + labTest.getTestDate());
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

        Label teacherLabel = new Label("Teacher: " + labTest.getTeacherName());
        teacherLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

        contentBox.getChildren().addAll(titleLabel, dateLabel, teacherLabel);

        if (labTest.getEvaluationCriteria() != null && !labTest.getEvaluationCriteria().isEmpty()) {
            Label criteriaLabel = new Label("Criteria: " + labTest.getEvaluationCriteria());
            criteriaLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #777;");
            criteriaLabel.setWrapText(true);
            contentBox.getChildren().add(criteriaLabel);
        }

        HBox.setHgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);
        box.getChildren().add(contentBox);

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