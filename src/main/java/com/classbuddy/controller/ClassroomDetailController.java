package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import com.classbuddy.util.ViewTransitions;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com.classbuddy.model.Exam;
import com.classbuddy.model.Notice;
import com.classbuddy.model.Routine;
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
    private Label classroomMetaLabel;
    @FXML
    private Label pageTitle;
    @FXML
    private Label pageSubtitle;
    @FXML
    private TabPane classroomTabs;
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
    @FXML
    private Button addRoutineBtn;
    @FXML
    private Button addExamBtn;
    @FXML
    private Button addNoticeBtn;
    @FXML
    private Button addCTQuizBtn;
    @FXML
    private Button addLabTestBtn;
    @FXML
    private Tab ctQuizTab;
    @FXML
    private Tab labTestTab;
    @FXML
    private Button ctQuizBtn;
    @FXML
    private Button labTestBtn;
    
    // New sidebar buttons
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnSchedule;
    @FXML
    private Button btnAddRoutine;
    @FXML
    private Button btnCalendar;
    @FXML
    private Button btnExams;
    @FXML
    private Button btnAddExam;
    @FXML
    private Button btnNotices;
    @FXML
    private Button btnAddNotice;
    @FXML
    private Button btnCTQuiz;
    @FXML
    private Button btnAddCTQuiz;
    @FXML
    private Button btnLabTests;
    @FXML
    private Button btnAddLabTest;

    private Classroom classroom;
    private User user;
    
    @FXML
    private IntegratedCalendarController integratedCalendarController;

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    public void initialize() {
        // Don't load data here - classroom and user are null
    }

    /**
     * IMPORTANT: Call this method AFTER setting classroom and user
     */
    public void loadData() {
        if (classroom != null) {
            classroomNameLabel.setText(classroom.getName());
            
            if (classroomMetaLabel != null) {
                classroomMetaLabel.setText("Section " + classroom.getSection() + " · " + classroom.getDepartment());
            }

            boolean isAdmin = user != null && user.getRole().name().equals("ADMIN");
            
            // Show/hide "Add" buttons on pages based on role
            if (addRoutineBtn != null) addRoutineBtn.setVisible(isAdmin);
            if (addExamBtn != null) addExamBtn.setVisible(isAdmin);
            if (addNoticeBtn != null) addNoticeBtn.setVisible(isAdmin);
            if (addCTQuizBtn != null) addCTQuizBtn.setVisible(isAdmin);
            if (addLabTestBtn != null) addLabTestBtn.setVisible(isAdmin);

            loadClassroomData();
            loadIntegratedCalendar();
            setupSidebarActiveStates();
        }
    }
    
    private void setupSidebarActiveStates() {
        // Listen to tab changes and update sidebar button active states
        if (classroomTabs != null) {
            classroomTabs.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
                updateSidebarActiveState(newVal.intValue());
            });
        }
    }
    
    private void updateSidebarActiveState(int tabIndex) {
        // Remove active class from all buttons
        Button[] buttons = {btnSchedule, btnCalendar, btnExams, btnNotices, btnCTQuiz, btnLabTests};
        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("sidebar-item-active");
            }
        }
        
        // Add active class to the selected button
        Button activeBtn = null;
        switch (tabIndex) {
            case 0: activeBtn = btnSchedule; break;
            case 1: activeBtn = btnCalendar; break;
            case 2: activeBtn = btnExams; break;
            case 3: activeBtn = btnNotices; break;
            case 4: activeBtn = btnCTQuiz; break;
            case 5: activeBtn = btnLabTests; break;
        }
        
        if (activeBtn != null && !activeBtn.getStyleClass().contains("sidebar-item-active")) {
            activeBtn.getStyleClass().add("sidebar-item-active");
        }
    }
    
    private void loadIntegratedCalendar() {
        if (integratedCalendarController != null && classroom != null) {
            integratedCalendarController.setClassroom(classroom);
        }
    }
    
    @FXML
    public void showRoutineTab() {
        if (classroomTabs != null) classroomTabs.getSelectionModel().select(0);
    }
    
    @FXML
    public void showCalendarTab() {
        if (classroomTabs != null) classroomTabs.getSelectionModel().select(1);
    }
    
    @FXML
    public void showExamsTab() {
        if (classroomTabs != null) classroomTabs.getSelectionModel().select(2);
    }
    
    @FXML
    public void showNoticesTab() {
        if (classroomTabs != null) classroomTabs.getSelectionModel().select(3);
    }
    
    @FXML
    public void showCTQuizTab() {
        if (classroomTabs != null) classroomTabs.getSelectionModel().select(4);
    }
    
    @FXML
    public void showLabTestTab() {
        if (classroomTabs != null) classroomTabs.getSelectionModel().select(5);
    }
    
    @FXML
    public void refreshRoutine() {
        loadRoutine();
        if (integratedCalendarController != null) integratedCalendarController.refresh();
    }
    
    @FXML
    public void refreshExams() {
        loadExams();
    }
    
    @FXML
    public void refreshNotices() {
        loadNotices();
    }
    
    private void updatePageTitle(String title, String subtitle) {
        if (pageTitle != null) pageTitle.setText(title);
        if (pageSubtitle != null) pageSubtitle.setText(subtitle);
    }

    private void loadClassroomData() {
        loadRoutine();
        loadExams();
        loadNotices();
    }

    private void loadRoutine() {
        routineContainer.getChildren().clear();

        List<Routine> routines = RoutineService.getWeeklyRoutine(classroom.getId());

        if (routines.isEmpty()) {
            Label emptyLabel = new Label("No routine set");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14;");
            routineContainer.getChildren().add(emptyLabel);
        } else {
            java.util.Map<String, java.util.List<Routine>> byDay =
                    new java.util.LinkedHashMap<>();

            for (Routine routine : routines) {
                byDay.computeIfAbsent(routine.getDay(),
                        k -> new java.util.ArrayList<>()).add(routine);
            }

            for (String day : byDay.keySet()) {
                Label dayLabel = new Label(day);
                dayLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
                routineContainer.getChildren().add(dayLabel);

                for (Routine routine : byDay.get(day)) {
                    HBox routineBox = createRoutineBox(routine);
                    routineContainer.getChildren().add(routineBox);
                }
            }
        }
    }

    private HBox createRoutineBox(Routine routine) {
        HBox box = new HBox(15);
        box.getStyleClass().add("card");

        VBox infoBox = new VBox(8);
        
        Label courseLabel = new Label(routine.getCourseName());
        courseLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
        
        String details = "Period " + routine.getPeriodNumber() + " · " + 
                routine.getTimeStart() + " - " + routine.getTimeEnd();
        if (routine.getRoom() != null && !routine.getRoom().isEmpty()) {
            details += " · Room " + routine.getRoom();
        }
        
        Label detailsLabel = new Label(details);
        detailsLabel.setStyle("-fx-font-size: 13; -fx-text-fill: -text-light;");
        
        infoBox.getChildren().addAll(courseLabel, detailsLabel);
        
        if (routine.getTeacherName() != null && !routine.getTeacherName().isEmpty()) {
            Label teacherLabel = new Label(routine.getTeacherName());
            teacherLabel.getStyleClass().add("badge-blue");
            teacherLabel.setStyle("-fx-background-radius: 12; -fx-padding: 4 12; -fx-font-size: 11;");
            infoBox.getChildren().add(teacherLabel);
        }
        
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        box.getChildren().add(infoBox);
        return box;
    }

    private void loadExams() {
        examsContainer.getChildren().clear();

        List<Exam> exams = ExamService.getClassroomExams(classroom.getId());

        if (exams.isEmpty()) {
            Label emptyLabel = new Label("No exams scheduled");
            emptyLabel.setStyle("-fx-text-fill: -text-light; -fx-font-size: 14;");
            examsContainer.getChildren().add(emptyLabel);
        } else {
            for (Exam exam : exams) {
                HBox examBox = createExamBox(exam);
                examsContainer.getChildren().add(examBox);
            }
        }
    }

    private HBox createExamBox(Exam exam) {
        HBox box = new HBox(15);
        box.setStyle(
                "-fx-background-color: -card-bg;" +
                        "-fx-border-color: -primary-orange;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        Label examLabel = new Label(
                exam.getCourseName() + " (" + exam.getExamType() + ")\n" +
                        exam.getExamDate() + " at " + exam.getExamTime() +
                        (exam.getRoom() != null && !exam.getRoom().isEmpty()
                                ? " | Room: " + exam.getRoom() : "")
        );
        examLabel.setStyle("-fx-font-size: 13;");
        examLabel.setWrapText(true);
        HBox.setHgrow(examLabel, javafx.scene.layout.Priority.ALWAYS);

        box.getChildren().add(examLabel);
        return box;
    }

    private void loadNotices() {
        noticesContainer.getChildren().clear();

        List<Notice> notices = NoticeService.getClassroomNotices(classroom.getId());

        if (notices.isEmpty()) {
            Label emptyLabel = new Label("No notices");
            emptyLabel.setStyle("-fx-text-fill: -text-light; -fx-font-size: 14;");
            noticesContainer.getChildren().add(emptyLabel);
        } else {
            for (Notice notice : notices) {
                VBox noticeBox = createNoticeBox(notice);
                noticesContainer.getChildren().add(noticeBox);
            }
        }
    }

    private VBox createNoticeBox(Notice notice) {
        VBox box = new VBox(8);
        box.setStyle(
                "-fx-background-color: " + (notice.isPinned() ? "#FEF2F2" : "-card-bg") + ";" +
                        "-fx-border-color: " + (notice.isPinned() ? "-error" : "-border-color") + ";" +
                        "-fx-border-width: " + (notice.isPinned() ? "2" : "1") + ";" +
                        "-fx-padding: 15;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        Label titleLabel = new Label(
                notice.getTitle()
        );
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Label categoryLabel = new Label("[" + notice.getCategory() + "]");
        categoryLabel.setStyle("-fx-font-size: 11; -fx-text-fill: -text-light;");

        Label contentLabel = new Label(notice.getContent());
        contentLabel.setStyle("-fx-font-size: 12;");
        contentLabel.setWrapText(true);

        Label timeLabel = new Label(notice.getCreatedAt().toString());
        timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: -text-light;");

        box.getChildren().addAll(titleLabel, categoryLabel, contentLabel, timeLabel);
        return box;
    }

    @FXML
    public void goToAddRoutine() {
        navigateToScreen("/fxml/add-routine.fxml", "AddRoutineController");
    }

    @FXML
    public void goToAddExam() {
        navigateToScreen("/fxml/add-exam.fxml", "AddExamController");
    }

    @FXML
    public void goToAddNotice() {
        navigateToScreen("/fxml/add-notice.fxml", "AddNoticeController");
    }

    @FXML
    public void goToAddCTQuiz() {
        navigateToScreen("/fxml/add-ctquiz.fxml", "AddCTQuizController");
    }

    @FXML
    public void goToAddLabTest() {
        navigateToScreen("/fxml/add-labtest.fxml", "AddLabTestController");
    }

    private void navigateToScreen(String fxmlPath, String controllerType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            // Set classroom using reflection or instanceof
            if (controller instanceof AddRoutineController) {
                AddRoutineController ctrl = (AddRoutineController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            } else if (controller instanceof AddExamController) {
                AddExamController ctrl = (AddExamController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            } else if (controller instanceof AddNoticeController) {
                AddNoticeController ctrl = (AddNoticeController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            } else if (controller instanceof AddCTQuizController) {
                AddCTQuizController ctrl = (AddCTQuizController) controller;
                ctrl.setClassroom(classroom);
            } else if (controller instanceof AddLabTestController) {
                AddLabTestController ctrl = (AddLabTestController) controller;
                ctrl.setClassroom(classroom);
            }

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
            stage.show();
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBackToDashboard() {
        try {
            FXMLLoader fxmlLoader;
            if (user.getRole().name().equals("ADMIN")) {
                fxmlLoader = new FXMLLoader(
                        getClass().getResource("/fxml/admin-dashboard.fxml")
                );
            } else {
                fxmlLoader = new FXMLLoader(
                        getClass().getResource("/fxml/student-dashboard.fxml")
                );
            }

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.centerOnScreen();
            stage.show();
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}