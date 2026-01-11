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
import com.classbuddy.model.CTQuiz;
import com.classbuddy.model.LabTest;
import com.classbuddy.model.Notice;
import com.classbuddy.model.Routine;
import com.classbuddy.model.User;
import com.classbuddy.service.ExamService;
import com.classbuddy.service.CTQuizService;
import com.classbuddy.service.LabTestService;
import com.classbuddy.service.NoticeService;
import com.classbuddy.service.RoutineService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
    private VBox examsContainer;
    @FXML
    private VBox noticesContainer;

    @FXML
    private Label classIdLabel;

    // CT/Quiz and Lab Tests are now shown under the Exams tab
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
    // Removed CT/Quiz and Lab Tests tabs and sidebar buttons in FXML
    
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
    private Button btnEvents;
    // Attendance features removed
    // Additional quick-add buttons on Schedule tab
    @FXML private Button addExamFromScheduleBtn;
    @FXML private Button addNoticeFromScheduleBtn;
    @FXML private Button addCTQuizFromScheduleBtn;
    @FXML private Button addLabTestFromScheduleBtn;

    private Classroom classroom;
    private User user;
    
    @FXML
    private IntegratedCalendarController integratedCalendarController;

    @FXML
    private TimetableGridController timetableGridController;

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

            if (classIdLabel != null) {
                String classIdText = classroom.getClassId() == null ? "" : classroom.getClassId();
                classIdLabel.setText("ID: " + classIdText);
                classIdLabel.setStyle("-fx-cursor: hand;");
                classIdLabel.setOnMouseClicked(ev -> {
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    content.putString(classIdText);
                    clipboard.setContent(content);
                    if (classroomMetaLabel != null) {
                        String old = classroomMetaLabel.getText();
                        classroomMetaLabel.setText("Class ID copied to clipboard");
                        new Thread(() -> {
                            try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                            javafx.application.Platform.runLater(() -> classroomMetaLabel.setText(old));
                        }).start();
                    }
                });
            }
            boolean isAdmin = user != null && user.getRole().name().equals("ADMIN");
            
            // Show/hide "Add" buttons on pages based on role
            if (addRoutineBtn != null) addRoutineBtn.setVisible(isAdmin);
            if (addExamBtn != null) addExamBtn.setVisible(isAdmin);
            if (addNoticeBtn != null) addNoticeBtn.setVisible(isAdmin);
            if (addCTQuizBtn != null) addCTQuizBtn.setVisible(isAdmin);
            if (addLabTestBtn != null) addLabTestBtn.setVisible(isAdmin);
            if (addExamFromScheduleBtn != null) addExamFromScheduleBtn.setVisible(isAdmin);
            if (addNoticeFromScheduleBtn != null) addNoticeFromScheduleBtn.setVisible(isAdmin);
            if (addCTQuizFromScheduleBtn != null) addCTQuizFromScheduleBtn.setVisible(isAdmin);
            if (addLabTestFromScheduleBtn != null) addLabTestFromScheduleBtn.setVisible(isAdmin);
            // Attendance features removed

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
        Button[] buttons = {btnSchedule, btnCalendar, btnExams, btnNotices};
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
    
    // CT/Quiz and Lab Tests tabs removed; content merged under Exams tab
    
    @FXML
    public void refreshRoutine() {
        loadRoutine();
        if (integratedCalendarController != null) integratedCalendarController.refresh();
    }

    // Attendance analytics removed
    
    @FXML
    public void refreshExams() {
        loadExams();
    }
    
    @FXML
    public void refreshNotices() {
        loadNotices();
    }

    private void loadClassroomData() {
        loadRoutine();
        loadExams();
        loadNotices();
    }

    private void loadRoutine() {
        if (classroom == null) return;
        List<Routine> routines = RoutineService.getWeeklyRoutine(classroom.getId());

        if (timetableGridController != null) {
            timetableGridController.setClassroom(classroom);
            timetableGridController.setRoutines(routines);
            return;
        }
    }

    private void loadExams() {
        examsContainer.getChildren().clear();

        // Exams
        Label examsHeader = new Label("Exams");
        examsHeader.getStyleClass().add("section-title");
        examsContainer.getChildren().add(examsHeader);

        List<Exam> exams = ExamService.getClassroomExams(classroom.getId());
        if (exams.isEmpty()) {
            Label emptyLabel = new Label("No exams scheduled");
            emptyLabel.getStyleClass().addAll("text-muted", "body-sm");
            examsContainer.getChildren().add(emptyLabel);
        } else {
            for (Exam exam : exams) {
                HBox examBox = createExamBox(exam);
                examsContainer.getChildren().add(examBox);
            }
        }

        // Tests (CT/Quiz)
        Label testsHeader = new Label("Tests");
        testsHeader.getStyleClass().add("section-title");
        examsContainer.getChildren().add(testsHeader);
        List<CTQuiz> tests = CTQuizService.getClassroomCTQuizzes(classroom.getId());
        if (tests.isEmpty()) {
            Label emptyTests = new Label("No tests added");
            emptyTests.getStyleClass().addAll("text-muted", "body-sm");
            examsContainer.getChildren().add(emptyTests);
        } else {
            for (CTQuiz test : tests) {
                HBox testBox = createCTQuizBox(test);
                examsContainer.getChildren().add(testBox);
            }
        }

        // Lab Tests
        Label labHeader = new Label("Lab Tests");
        labHeader.getStyleClass().add("section-title");
        examsContainer.getChildren().add(labHeader);
        List<LabTest> labs = LabTestService.getClassroomLabTests(classroom.getId());
        if (labs.isEmpty()) {
            Label emptyLabs = new Label("No lab tests added");
            emptyLabs.getStyleClass().addAll("text-muted", "body-sm");
            examsContainer.getChildren().add(emptyLabs);
        } else {
            for (LabTest lab : labs) {
                HBox labBox = createLabTestBox(lab);
                examsContainer.getChildren().add(labBox);
            }
        }
    }

    private HBox createExamBox(Exam exam) {
        HBox box = new HBox(15);
        box.getStyleClass().addAll("inline-card", "list-card-accent");
        box.setStyle("-event-color: -primary-orange;");

        Label examLabel = new Label(
                exam.getCourseName() + " (" + exam.getExamType() + ")\n" +
                        exam.getExamDate() + " at " + exam.getExamTime() +
                        (exam.getRoom() != null && !exam.getRoom().isEmpty()
                                ? " | Room: " + exam.getRoom() : "")
        );
                    examLabel.getStyleClass().add("body-sm");
        examLabel.setWrapText(true);
        HBox.setHgrow(examLabel, javafx.scene.layout.Priority.ALWAYS);

        box.getChildren().add(examLabel);

        // admin-only context menu: edit/delete exam
        boolean isAdminExam = user != null && user.getRole().name().equals("ADMIN");
        if (isAdminExam) {
            box.addEventFilter(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
                ContextMenu menu = new ContextMenu();
                MenuItem edit = new MenuItem("Edit Exam");
                edit.setOnAction(ev -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-exam.fxml"));
                        Parent root = loader.load();
                        EditExamController ctrl = loader.getController();
                        ctrl.setClassroom(classroom);
                        ctrl.setExam(exam);
                        ctrl.loadData();

                        Scene scene = new Scene(root, 1366, 800);
                        Stage stage = (Stage) box.getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                        ViewTransitions.fadeIn(root);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                MenuItem delete = new MenuItem("Delete Exam");
                delete.setOnAction(ev -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Exam");
                    confirm.setHeaderText("Delete this exam?");
                    confirm.setContentText(exam.getCourseName());
                    Optional<ButtonType> res = confirm.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.OK) {
                        if (ExamService.deleteExam(exam.getId())) {
                            loadExams();
                        }
                    }
                });

                menu.getItems().addAll(edit, delete);
                menu.show(box, e.getScreenX(), e.getScreenY());
                e.consume();
            });
        }
        return box;
    }

    private HBox createCTQuizBox(CTQuiz test) {
        HBox box = new HBox(15);
        box.getStyleClass().add("inline-card");

        String title = (test.getName() != null && !test.getName().isEmpty()) ? test.getName() : "Test";
        String deadline = (test.getDeadline() != null) ? (" | Due: " + test.getDeadline()) : "";
        Label label = new Label(title + deadline);
        label.getStyleClass().add("body-sm");
        label.setWrapText(true);
        HBox.setHgrow(label, javafx.scene.layout.Priority.ALWAYS);
        box.getChildren().add(label);

        // admin-only context menu: edit/delete test
        boolean isAdminTest = user != null && user.getRole().name().equals("ADMIN");
        if (isAdminTest) {
            box.addEventFilter(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
                ContextMenu menu = new ContextMenu();
                MenuItem edit = new MenuItem("Edit Test");
                edit.setOnAction(ev -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-ctquiz.fxml"));
                        Parent root = loader.load();
                        EditCTQuizController ctrl = loader.getController();
                        ctrl.setClassroom(classroom);
                        ctrl.setCtQuiz(test);
                        ctrl.loadData();

                        Scene scene = new Scene(root, 1366, 800);
                        Stage stage = (Stage) box.getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                        ViewTransitions.fadeIn(root);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                MenuItem delete = new MenuItem("Delete Test");
                delete.setOnAction(ev -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Test");
                    confirm.setHeaderText("Delete this test?");
                    confirm.setContentText(test.getName());
                    Optional<ButtonType> res = confirm.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.OK) {
                        if (CTQuizService.deleteCTQuiz(test.getId())) {
                            loadExams();
                        }
                    }
                });

                menu.getItems().addAll(edit, delete);
                menu.show(box, e.getScreenX(), e.getScreenY());
                e.consume();
            });
        }

        return box;
    }

    private HBox createLabTestBox(LabTest lab) {
        HBox box = new HBox(15);
        box.getStyleClass().add("inline-card");

        String title = "Experiment " + lab.getExperimentNumber();
        String date = (lab.getTestDate() != null) ? (" | Date: " + lab.getTestDate()) : "";
        Label label = new Label(title + date);
        label.getStyleClass().add("body-sm");
        label.setWrapText(true);
        HBox.setHgrow(label, javafx.scene.layout.Priority.ALWAYS);
        box.getChildren().add(label);

        // admin-only context menu: edit/delete lab test
        boolean isAdminLab = user != null && user.getRole().name().equals("ADMIN");
        if (isAdminLab) {
            box.addEventFilter(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
                ContextMenu menu = new ContextMenu();
                MenuItem edit = new MenuItem("Edit Lab Test");
                edit.setOnAction(ev -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-labtest.fxml"));
                        Parent root = loader.load();
                        EditLabTestController ctrl = loader.getController();
                        ctrl.setClassroom(classroom);
                        ctrl.setLabTest(lab);
                        ctrl.loadData();

                        Scene scene = new Scene(root, 1366, 800);
                        Stage stage = (Stage) box.getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                        ViewTransitions.fadeIn(root);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                MenuItem delete = new MenuItem("Delete Lab Test");
                delete.setOnAction(ev -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Lab Test");
                    confirm.setHeaderText("Delete this lab test?");
                    confirm.setContentText("Experiment " + lab.getExperimentNumber());
                    Optional<ButtonType> res = confirm.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.OK) {
                        if (LabTestService.deleteLabTest(lab.getId())) {
                            loadExams();
                        }
                    }
                });

                menu.getItems().addAll(edit, delete);
                menu.show(box, e.getScreenX(), e.getScreenY());
                e.consume();
            });
        }

        return box;
    }

    private void loadNotices() {
        noticesContainer.getChildren().clear();

        List<Notice> notices = NoticeService.getClassroomNotices(classroom.getId());

        if (notices.isEmpty()) {
            Label emptyLabel = new Label("No notices");
            emptyLabel.getStyleClass().addAll("text-muted", "body-sm");
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
        box.getStyleClass().addAll("inline-card", "notice-card");
        if (notice.isPinned()) {
            box.getStyleClass().add("notice-card-pinned");
        }

        Label titleLabel = new Label(
                notice.getTitle()
        );
        titleLabel.getStyleClass().add("heading-xs");

        Label categoryLabel = new Label("[" + notice.getCategory() + "]");
        categoryLabel.getStyleClass().addAll("text-muted", "body-xs");

        Label contentLabel = new Label(notice.getContent());
        contentLabel.getStyleClass().add("body-sm");
        contentLabel.setWrapText(true);

        Label timeLabel = new Label(notice.getCreatedAt().toString());
        timeLabel.getStyleClass().addAll("text-muted", "body-xs");

        box.getChildren().addAll(titleLabel, categoryLabel, contentLabel, timeLabel);

        // Context menu: edit / delete notice (admin-only)
        boolean isAdmin = user != null && user.getRole().name().equals("ADMIN");
        if (isAdmin) {
            box.addEventFilter(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
                ContextMenu menu = new ContextMenu();

                MenuItem edit = new MenuItem("Edit Notice");
                edit.setOnAction(ev -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-notice.fxml"));
                        Parent root = loader.load();
                        EditNoticeController ctrl = loader.getController();
                        ctrl.setClassroom(classroom);
                        ctrl.setNotice(notice);
                        ctrl.loadData();

                        Scene scene = new Scene(root, 1366, 800);
                        Stage stage = (Stage) box.getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                        ViewTransitions.fadeIn(root);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                MenuItem delete = new MenuItem("Delete Notice");
                delete.setOnAction(ev -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Notice");
                    confirm.setHeaderText("Delete this notice?");
                    confirm.setContentText(notice.getTitle());

                    Optional<ButtonType> res = confirm.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.OK) {
                        if (NoticeService.deleteNotice(notice.getId())) {
                            loadNotices();
                        }
                    }
                });

                menu.getItems().addAll(edit, delete);
                // Show menu at cursor position
                menu.show(box, e.getScreenX(), e.getScreenY());
                e.consume();
            });
        }

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

    @FXML
    public void goToEvents() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/classroom-events.fxml"));
            Parent root = loader.load();
            ClassroomEventsController ctrl = loader.getController();
            ctrl.setClassroom(classroom);
            ctrl.setUser(user);
            ctrl.loadData();
            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1366);
            stage.setMinHeight(800);
            stage.setWidth(1366);
            stage.setHeight(800);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Attendance marker removed

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

            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1366);
            stage.setMinHeight(800);
            stage.setWidth(1366);
            stage.setHeight(800);
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
            if (user != null && user.getRole().name().equals("ADMIN")) {
                fxmlLoader = new FXMLLoader(
                        getClass().getResource("/fxml/admin-dashboard.fxml")
                );
            } else {
                fxmlLoader = new FXMLLoader(
                        getClass().getResource("/fxml/student-dashboard.fxml")
                );
            }

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1366);
            stage.setMinHeight(800);
            stage.setWidth(1366);
            stage.setHeight(800);
            stage.show();
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}