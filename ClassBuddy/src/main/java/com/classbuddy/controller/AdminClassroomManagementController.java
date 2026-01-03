package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.classbuddy.util.ViewTransitions;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.classbuddy.model.*;
import com.classbuddy.service.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Admin Classroom Management - Full CRUD operations for all entities
 */
public class AdminClassroomManagementController {
    @FXML
    private Label classroomNameLabel;
    @FXML
    private TabPane managementTabs;
    @FXML
    private VBox routineManagementBox;
    @FXML
    private VBox examManagementBox;
    @FXML
    private VBox ctQuizManagementBox;
    @FXML
    private VBox labTestManagementBox;
    @FXML
    private VBox noticeManagementBox;

    private Classroom classroom;
    private User admin;

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroomNameLabel != null) {
            classroomNameLabel.setText("Manage: " + classroom.getName());
        }
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    @FXML
    public void initialize() {
        if (classroom != null) {
            loadAllData();
        }
    }

    /**
     * Load all management data
     */
    private void loadAllData() {
        loadRoutineManagement();
        loadExamManagement();
        loadCTQuizManagement();
        loadLabTestManagement();
        loadNoticeManagement();
    }

    /**
     * Routine Management
     */
    private void loadRoutineManagement() {
        if (routineManagementBox == null) return;
        routineManagementBox.getChildren().clear();

        // Add button
        Button addBtn = new Button("Add Routine Period");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> handleAddRoutine());

        routineManagementBox.getChildren().add(addBtn);

        // List routines
        List<Routine> routines = RoutineService.getWeeklyRoutine(classroom.getId());
        for (Routine routine : routines) {
            routineManagementBox.getChildren().add(createRoutineCard(routine));
        }
    }

    private HBox createRoutineCard(Routine routine) {
        HBox card = new HBox(10);
        card.setStyle("-fx-padding: 12; -fx-border-color: -border-color; -fx-border-width: 1; -fx-background-color: -card-bg; -fx-background-radius: 12; -fx-border-radius: 12;");

        Label info = new Label(
                routine.getDay() + " - Period " + routine.getPeriodNumber() + ": " +
                routine.getCourseName() + " (" + routine.getTimeStart() + "-" + routine.getTimeEnd() + ")"
        );
        info.setStyle("-fx-font-size: 12;");

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(e -> handleDeleteRoutine(routine));

        card.getChildren().addAll(info, deleteBtn);
        return card;
    }

    private void handleAddRoutine() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-routine.fxml"));
            Parent root = loader.load();

            AddRoutineController controller = loader.getController();
            controller.setClassroom(classroom);
            controller.setOnSuccess(this::loadRoutineManagement);


            Stage stage = new Stage();
            stage.setTitle("Add Routine Period");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteRoutine(Routine routine) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Routine");
        confirm.setHeaderText("Delete this routine period?");
        confirm.setContentText(routine.toString());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (RoutineService.deleteRoutine(routine.getId())) {
                loadRoutineManagement();
            }
        }
    }

    /**
     * Exam Management
     */
    private void loadExamManagement() {
        if (examManagementBox == null) return;
        examManagementBox.getChildren().clear();

        Button addBtn = new Button("Add Exam");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> handleAddExam());

        examManagementBox.getChildren().add(addBtn);

        List<Exam> exams = ExamService.getClassroomExams(classroom.getId());
        for (Exam exam : exams) {
            examManagementBox.getChildren().add(createExamCard(exam));
        }
    }

    private HBox createExamCard(Exam exam) {
        HBox card = new HBox(10);
        card.setStyle("-fx-padding: 12; -fx-border-color: -border-color; -fx-border-width: 1; -fx-background-color: -card-bg; -fx-background-radius: 12; -fx-border-radius: 12;");

        Label info = new Label(
                exam.getCourseName() + " (" + exam.getExamType() + ") - " +
                exam.getExamDate() + " at " + exam.getExamTime()
        );
        info.setStyle("-fx-font-size: 12;");

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(e -> handleDeleteExam(exam));

        card.getChildren().addAll(info, deleteBtn);
        return card;
    }

    private void handleAddExam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-exam.fxml"));
            Parent root = loader.load();

            AddExamController controller = loader.getController();
            controller.setClassroom(classroom);
            controller.setOnSuccess(this::loadExamManagement);


            Stage stage = new Stage();
            stage.setTitle("Add Exam");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteExam(Exam exam) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Exam");
        confirm.setHeaderText("Delete this exam?");
        confirm.setContentText(exam.toString());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (ExamService.deleteExam(exam.getId())) {
                loadExamManagement();
            }
        }
    }

    /**
     * CT/Quiz Management
     */
    private void loadCTQuizManagement() {
        if (ctQuizManagementBox == null) return;
        ctQuizManagementBox.getChildren().clear();

        Button addBtn = new Button("Add CT/Quiz");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> handleAddCTQuiz());

        ctQuizManagementBox.getChildren().add(addBtn);

        List<CTQuiz> ctQuizzes = CTQuizService.getClassroomCTQuizzes(classroom.getId());
        for (CTQuiz ctQuiz : ctQuizzes) {
            ctQuizManagementBox.getChildren().add(createCTQuizCard(ctQuiz));
        }
    }

    private HBox createCTQuizCard(CTQuiz ctQuiz) {
        HBox card = new HBox(10);
        card.setStyle("-fx-padding: 12; -fx-border-color: -border-color; -fx-border-width: 1; -fx-background-color: -card-bg; -fx-background-radius: 12; -fx-border-radius: 12;");

        Label info = new Label(
                ctQuiz.getName() + " - Deadline: " + ctQuiz.getDeadline() +
                (ctQuiz.isCompleted() ? " (Completed)" : "")
        );
        info.setStyle("-fx-font-size: 12;");

        CheckBox completedCB = new CheckBox("Completed");
        completedCB.setSelected(ctQuiz.isCompleted());
        completedCB.setOnAction(e -> {
            CTQuizService.markAsCompleted(ctQuiz.getId(), completedCB.isSelected());
            loadCTQuizManagement();
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(e -> handleDeleteCTQuiz(ctQuiz));

        card.getChildren().addAll(info, completedCB, deleteBtn);
        return card;
    }

    private void handleAddCTQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-ctquiz.fxml"));
            Parent root = loader.load();

            AddCTQuizController controller = loader.getController();
            controller.setClassroomId(classroom.getId());
            controller.setOnSuccess(this::loadCTQuizManagement);

            Stage stage = new Stage();
            stage.setTitle("Add CT/Quiz");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteCTQuiz(CTQuiz ctQuiz) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete CT/Quiz");
        confirm.setHeaderText("Delete this CT/Quiz?");
        confirm.setContentText(ctQuiz.toString());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (CTQuizService.deleteCTQuiz(ctQuiz.getId())) {
                loadCTQuizManagement();
            }
        }
    }

    /**
     * Lab Test Management
     */
    private void loadLabTestManagement() {
        if (labTestManagementBox == null) return;
        labTestManagementBox.getChildren().clear();

        Button addBtn = new Button("Add Lab Test");
        addBtn.getStyleClass().addAll("btn", "btn-secondary");
        addBtn.setOnAction(e -> handleAddLabTest());

        labTestManagementBox.getChildren().add(addBtn);

        List<LabTest> labTests = LabTestService.getClassroomLabTests(classroom.getId());
        for (LabTest labTest : labTests) {
            labTestManagementBox.getChildren().add(createLabTestCard(labTest));
        }
    }

    private HBox createLabTestCard(LabTest labTest) {
        HBox card = new HBox(10);
        card.setStyle("-fx-padding: 12; -fx-border-color: -border-color; -fx-border-width: 1; -fx-background-color: -card-bg; -fx-background-radius: 12; -fx-border-radius: 12;");

        Label info = new Label(
                "Experiment " + labTest.getExperimentNumber() + " - " +
                labTest.getTestDate() + " (Teacher: " + labTest.getTeacherName() + ")"
        );
        info.setStyle("-fx-font-size: 12;");

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(e -> handleDeleteLabTest(labTest));

        card.getChildren().addAll(info, deleteBtn);
        return card;
    }

    private void handleAddLabTest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-labtest.fxml"));
            Parent root = loader.load();

            AddLabTestController controller = loader.getController();
            controller.setClassroomId(classroom.getId());
            controller.setOnSuccess(this::loadLabTestManagement);

            Stage stage = new Stage();
            stage.setTitle("Add Lab Test");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteLabTest(LabTest labTest) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Lab Test");
        confirm.setHeaderText("Delete this lab test?");
        confirm.setContentText(labTest.toString());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (LabTestService.deleteLabTest(labTest.getId())) {
                loadLabTestManagement();
            }
        }
    }

    /**
     * Notice Management
     */
    private void loadNoticeManagement() {
        if (noticeManagementBox == null) return;
        noticeManagementBox.getChildren().clear();

        Button addBtn = new Button("Post Notice");
        addBtn.getStyleClass().addAll("btn", "btn-primary");
        addBtn.setOnAction(e -> handleAddNotice());

        noticeManagementBox.getChildren().add(addBtn);

        List<Notice> notices = NoticeService.getClassroomNotices(classroom.getId());
        for (Notice notice : notices) {
            noticeManagementBox.getChildren().add(createNoticeCard(notice));
        }
    }

    private VBox createNoticeCard(Notice notice) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 12; -fx-border-color: -border-color; -fx-border-width: 1; -fx-background-color: -card-bg; -fx-background-radius: 12; -fx-border-radius: 12;");

        Label title = new Label(notice.getTitle());
        title.setStyle("-fx-font-weight: bold;");

        Label content = new Label(notice.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 11;");

        HBox actions = new HBox(10);
        CheckBox pinCB = new CheckBox("Pinned");
        pinCB.setSelected(notice.isPinned());
        pinCB.setOnAction(e -> {
            NoticeService.togglePin(notice.getId(), pinCB.isSelected());
            loadNoticeManagement();
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(e -> handleDeleteNotice(notice));

        actions.getChildren().addAll(pinCB, deleteBtn);

        card.getChildren().addAll(title, content, actions);
        return card;
    }

    private void handleAddNotice() {
        // Simple dialog for adding notice
        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Add Notice");
        titleDialog.setHeaderText("Enter notice title");
        Optional<String> titleResult = titleDialog.showAndWait();

        if (titleResult.isPresent() && !titleResult.get().trim().isEmpty()) {
            TextInputDialog contentDialog = new TextInputDialog();
            contentDialog.setTitle("Add Notice");
            contentDialog.setHeaderText("Enter notice content");
            Optional<String> contentResult = contentDialog.showAndWait();

            if (contentResult.isPresent()) {
                String[] categories = {"General", "Routine", "Exam", "CT"};
                ChoiceDialog<String> categoryDialog = new ChoiceDialog<>("General", categories);
                categoryDialog.setTitle("Add Notice");
                categoryDialog.setHeaderText("Select category");
                Optional<String> categoryResult = categoryDialog.showAndWait();

                String category = categoryResult.orElse("General");

                boolean success = NoticeService.postNotice(
                        classroom.getId(),
                        titleResult.get().trim(),
                        contentResult.get().trim(),
                        category,
                        admin.getId()
                );

                if (success) {
                    loadNoticeManagement();
                }
            }
        }
    }

    private void handleDeleteNotice(Notice notice) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Notice");
        confirm.setHeaderText("Delete this notice?");
        confirm.setContentText(notice.getTitle());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (NoticeService.deleteNotice(notice.getId())) {
                loadNoticeManagement();
            }
        }
    }

    @FXML
    public void goBack() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
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
