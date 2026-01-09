package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.model.CTQuiz;
import com.classbuddy.model.Exam;
import com.classbuddy.model.LabTest;
import com.classbuddy.model.Notice;
import com.classbuddy.model.Routine;
import com.classbuddy.service.CTQuizService;
import com.classbuddy.service.ExamService;
import com.classbuddy.service.LabTestService;
import com.classbuddy.service.NoticeService;
import com.classbuddy.service.RoutineService;
import com.classbuddy.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.classbuddy.util.ViewTransitions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class ClassroomEventsController {

    @FXML private Label classroomNameLabel;
    @FXML private Label classroomMetaLabel;
    @FXML private Label pageTitleLabel;
    @FXML private TextField courseFilterField;
    @FXML private TextField teacherFilterField;
    @FXML private ComboBox<String> typeFilterBox;
    @FXML private ComboBox<String> examTypeFilterBox;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private VBox cardsContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox filtersHeader;
    @FXML private GridPane filterPanel;

    private Classroom classroom;
    private User user;

    private record EventCard(String type, String title, String subtitle, LocalDate date, String meta) {}

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroom != null) {
            if (classroomNameLabel != null) classroomNameLabel.setText(classroom.getName());
            if (classroomMetaLabel != null) classroomMetaLabel.setText("Section " + classroom.getSection() + " · " + classroom.getDepartment());
            if (pageTitleLabel != null) pageTitleLabel.setText("Filter");
        }
    }

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    public void initialize() {
        if (typeFilterBox != null) {
            typeFilterBox.getItems().setAll("All", "Exam", "Quiz", "Lab", "Routine", "Notice");
            typeFilterBox.setValue("All");
        }
        if (examTypeFilterBox != null) {
            examTypeFilterBox.getItems().setAll("All", "Mid", "Final", "Viva", "CT", "Quiz", "Lab Test");
            examTypeFilterBox.setValue("All");
        }
        attachAutoRefresh();

        if (filtersHeader != null) {
            filtersHeader.setVisible(true);
            filtersHeader.setManaged(true);
        }
        if (filterPanel != null) {
            filterPanel.setVisible(true);
            filterPanel.setManaged(true);
        }
    }

    public void loadData() {
        refreshCards();
    }

    private void attachAutoRefresh() {
        if (courseFilterField != null) {
            courseFilterField.textProperty().addListener((obs, o, n) -> refreshCards());
        }
        if (teacherFilterField != null) {
            teacherFilterField.textProperty().addListener((obs, o, n) -> refreshCards());
        }
        if (typeFilterBox != null) {
            typeFilterBox.valueProperty().addListener((obs, o, n) -> refreshCards());
        }
        if (examTypeFilterBox != null) {
            examTypeFilterBox.valueProperty().addListener((obs, o, n) -> refreshCards());
        }
        if (fromDatePicker != null) {
            fromDatePicker.valueProperty().addListener((obs, o, n) -> refreshCards());
        }
        if (toDatePicker != null) {
            toDatePicker.valueProperty().addListener((obs, o, n) -> refreshCards());
        }
    }

    @FXML
    private void refreshCards() {
        if (classroom == null || cardsContainer == null) return;
        cardsContainer.getChildren().clear();
        List<EventCard> events = collectEvents();
        String courseFilter = safeLower(courseFilterField);
        String teacherFilter = safeLower(teacherFilterField);
        String typeFilter = typeFilterBox != null ? typeFilterBox.getValue() : "All";
        String examTypeFilter = examTypeFilterBox != null ? examTypeFilterBox.getValue() : "All";
        LocalDate from = fromDatePicker != null ? fromDatePicker.getValue() : null;
        LocalDate to = toDatePicker != null ? toDatePicker.getValue() : null;

        Map<String, List<EventCard>> grouped = new LinkedHashMap<>();
        for (EventCard card : events) {
            if (!"All".equalsIgnoreCase(typeFilter) && !card.type.equalsIgnoreCase(typeFilter)) continue;
            if (!courseFilter.isEmpty() && !card.title.toLowerCase(Locale.ROOT).contains(courseFilter)) continue;
            if (!teacherFilter.isEmpty() && (card.subtitle == null || !card.subtitle.toLowerCase(Locale.ROOT).contains(teacherFilter))) continue;
            if (card.type.equalsIgnoreCase("Exam") && !"All".equalsIgnoreCase(examTypeFilter)) {
                if (card.meta == null || !card.meta.toLowerCase(Locale.ROOT).contains(examTypeFilter.toLowerCase(Locale.ROOT))) continue;
            }
            if (from != null && card.date != null && card.date.isBefore(from)) continue;
            if (to != null && card.date != null && card.date.isAfter(to)) continue;
            grouped.computeIfAbsent(card.type, k -> new ArrayList<>()).add(card);
        }

        if (grouped.isEmpty()) {
            Label empty = new Label("No items match these filters.");
            empty.getStyleClass().add("text-muted");
            cardsContainer.getChildren().add(empty);
            return;
        }

        String[] order = {"Exam", "Quiz", "Lab", "Notice", "Routine"};
        for (String key : order) {
            List<EventCard> list = grouped.get(key);
            if (list == null || list.isEmpty()) continue;
            Label header = new Label(key);
            header.getStyleClass().add("section-title");
            FlowPane grid = buildGrid();
            list.stream().map(this::buildCard).forEach(grid.getChildren()::add);
            cardsContainer.getChildren().addAll(header, grid);
        }
    }

    private List<EventCard> collectEvents() {
        List<EventCard> list = new ArrayList<>();
        int cid = classroom != null ? classroom.getId() : -1;
        // Exams
        for (Exam ex : ExamService.getClassroomExams(cid)) {
            list.add(new EventCard("Exam", ex.getCourseName(), ex.getExamType(), ex.getExamDate(), ex.getExamTime() != null ? ex.getExamTime().toString() : null));
        }
        // CT/Quiz
        for (CTQuiz quiz : CTQuizService.getClassroomCTQuizzes(cid)) {
            list.add(new EventCard("Quiz", quiz.getName(), quiz.getDeadline() != null ? "Due " + quiz.getDeadline() : null, quiz.getDeadline(), null));
        }
        // Lab Tests
        for (LabTest lab : LabTestService.getClassroomLabTests(cid)) {
            list.add(new EventCard("Lab", "Experiment " + lab.getExperimentNumber(), lab.getTeacherName(), lab.getTestDate(), null));
        }
        // Notices
        for (Notice notice : NoticeService.getClassroomNotices(cid)) {
            list.add(new EventCard("Notice", notice.getTitle(), notice.getCategory(), notice.getCreatedAt() != null ? notice.getCreatedAt().toLocalDate() : null, null));
        }
        // Routine (no specific date)
        for (Routine rt : RoutineService.getWeeklyRoutine(cid)) {
            String subtitle = (rt.getTeacherName() != null ? rt.getTeacherName() + " · " : "") + (rt.getDay() != null ? rt.getDay() : "");
            list.add(new EventCard("Routine", rt.getCourseName(), subtitle, null, null));
        }
        return list;
    }

    private FlowPane buildGrid() {
        FlowPane grid = new FlowPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPrefWrapLength(1040);
        grid.getStyleClass().add("cards-grid");
        return grid;
    }

    private VBox buildCard(EventCard card) {
        VBox box = new VBox(4);
        box.getStyleClass().add("inline-card");
        box.setMinWidth(420);
        box.setPrefWidth(440);
        box.setMaxWidth(520);
        Label title = new Label(card.title);
        title.getStyleClass().add("card-title");
        Label type = new Label(card.type);
        type.getStyleClass().add("text-muted");
        box.getChildren().addAll(title, type);
        if (card.subtitle != null && !card.subtitle.isBlank()) {
            Label sub = new Label(card.subtitle);
            sub.getStyleClass().add("body-sm");
            box.getChildren().add(sub);
        }
        if (card.date != null) {
            Label date = new Label(card.date.toString());
            date.getStyleClass().add("body-sm");
            box.getChildren().add(date);
        }
        if (card.meta != null && !card.meta.isBlank()) {
            Label meta = new Label(card.meta);
            meta.getStyleClass().add("body-xs");
            box.getChildren().add(meta);
        }
        return box;
    }

    private String safeLower(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim().toLowerCase(Locale.ROOT) : "";
    }

    @FXML
    private void clearFilters() {
        if (courseFilterField != null) courseFilterField.clear();
        if (teacherFilterField != null) teacherFilterField.clear();
        if (typeFilterBox != null) typeFilterBox.setValue("All");
        if (examTypeFilterBox != null) examTypeFilterBox.setValue("All");
        if (fromDatePicker != null) fromDatePicker.setValue(null);
        if (toDatePicker != null) toDatePicker.setValue(null);
        refreshCards();
    }

    @FXML
    private void goBack() {
        goToClassroomDetail(ctrl -> {});
    }

    @FXML
    private void goToSchedule() {
        goToClassroomDetail(ClassroomDetailController::showRoutineTab);
    }

    @FXML
    private void goToCalendar() {
        goToClassroomDetail(ClassroomDetailController::showCalendarTab);
    }

    @FXML
    private void goToExams() {
        goToClassroomDetail(ClassroomDetailController::showExamsTab);
    }

    @FXML
    private void goToNotices() {
        goToClassroomDetail(ClassroomDetailController::showNoticesTab);
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader fxmlLoader;
            User current = user != null ? user : LoginController.getCurrentUser();
            if (current != null && current.getRole().name().equals("ADMIN")) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            } else {
                fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/student-dashboard.fxml"));
            }
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
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

    private void goToClassroomDetail(Consumer<ClassroomDetailController> afterLoad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/classroom-detail.fxml"));
            Parent root = loader.load();
            ClassroomDetailController ctrl = loader.getController();
            ctrl.setClassroom(classroom);
            ctrl.setUser(user != null ? user : LoginController.getCurrentUser());
            ctrl.loadData();
            if (afterLoad != null) afterLoad.accept(ctrl);
            Scene scene = new Scene(root, 1366, 800);
            Stage stage = (Stage) cardsContainer.getScene().getWindow();
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
