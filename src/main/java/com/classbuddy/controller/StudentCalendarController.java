package com.classbuddy.controller;

import com.classbuddy.model.CalendarEvent;
import com.classbuddy.model.Classroom;
import com.classbuddy.model.Routine;
import com.classbuddy.model.User;
import com.classbuddy.service.CalendarService;
import com.classbuddy.service.ClassroomService;
import com.classbuddy.service.RoutineService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.classbuddy.util.ViewTransitions;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class StudentCalendarController {

    @FXML private Label studentNameLabel;
    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox eventDetailsPane;
    @FXML private Label selectedDateLabel;
    @FXML private VBox eventsList;

    private User currentStudent;
    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private List<Classroom> studentClassrooms;

    @FXML
    public void initialize() {
        currentStudent = LoginController.getCurrentUser();
        
        if (currentStudent != null) {
            studentNameLabel.setText(currentStudent.getUsername());
            studentClassrooms = ClassroomService.getStudentClassrooms(currentStudent.getId());
        }

        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        updateCalendar();
        showEventsForDate(selectedDate);
    }

    @FXML
    public void goToDashboard() {
        navigateToView("/fxml/student-dashboard.fxml");
    }

    @FXML
    public void goToCalendar() {
        updateCalendar();
    }

    @FXML
    public void goToJoinClassroom() {
        navigateToView("/fxml/join-classroom.fxml");
    }

    @FXML
    public void goToProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(currentStudent, "/fxml/student-calendar.fxml");

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            System.err.println("Failed to load profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 900, 600);
            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);

            LoginController.setCurrentUser(null);
        } catch (IOException e) {
            System.err.println("Logout failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleToday() {
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        updateCalendar();
        showEventsForDate(selectedDate);
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        String monthName = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        monthYearLabel.setText(monthName + " " + currentYearMonth.getYear());

        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < dayNames.length; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.getStyleClass().add("calendar-day-header");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        LocalDate date = firstOfMonth.minusDays(dayOfWeek);
        int row = 1;
        int col = 0;

        while (date.isBefore(currentYearMonth.atEndOfMonth().plusDays(1)) || col != 0) {
            LocalDate cellDate = date;
            VBox dayCell = createDayCell(cellDate);
            calendarGrid.add(dayCell, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
            date = date.plusDays(1);
        }
    }

    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox(4);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.getStyleClass().add("calendar-day");
        cell.setMinHeight(90);
        cell.setMaxHeight(110);
        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            showEventsForDate(date);
            updateCalendar();
        });

        if (date.getMonth() != currentYearMonth.getMonth()) {
            cell.getStyleClass().add("calendar-day-other-month");
        }

        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("calendar-day-today");
        }

        if (date.equals(selectedDate)) {
            cell.setStyle(cell.getStyle() + "; -fx-border-color: -primary-orange; -fx-border-width: 2;");
        }

        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayNumber);

        List<CalendarEvent> dayEvents = getEventsForDate(date);
        List<Routine> dayRoutines = getRoutinesForDate(date);

        int displayCount = 0;
        int maxDisplay = 2;

        for (CalendarEvent event : dayEvents) {
            if (displayCount >= maxDisplay) break;
            HBox eventBox = createEventIndicator(event.getTitle(), event.getColor());
            cell.getChildren().add(eventBox);
            displayCount++;
        }

        for (Routine routine : dayRoutines) {
            if (displayCount >= maxDisplay) break;
            HBox eventBox = createEventIndicator(routine.getCourseName(), "#f97316");
            cell.getChildren().add(eventBox);
            displayCount++;
        }

        int totalItems = dayEvents.size() + dayRoutines.size();
        if (totalItems > maxDisplay) {
            Label moreLabel = new Label("+" + (totalItems - maxDisplay) + " more");
            moreLabel.setStyle("-fx-text-fill: -text-light; -fx-font-size: 9;");
            cell.getChildren().add(moreLabel);
        }

        return cell;
    }

    private HBox createEventIndicator(String title, String color) {
        HBox box = new HBox(3);
        box.getStyleClass().add("calendar-event");
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle("-fx-border-color: " + color + "; -fx-background-color: " + color + "22;");

        Label label = new Label(title.length() > 12 ? title.substring(0, 10) + ".." : title);
        label.getStyleClass().add("calendar-event-text");
        label.setStyle("-fx-font-size: 9;");
        box.getChildren().add(label);

        return box;
    }

    private void showEventsForDate(LocalDate date) {
        selectedDateLabel.setText(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        eventsList.getChildren().clear();

        List<CalendarEvent> events = getEventsForDate(date);
        List<Routine> routines = getRoutinesForDate(date);

        if (events.isEmpty() && routines.isEmpty()) {
            Label emptyLabel = new Label("No events scheduled for this day");
            emptyLabel.getStyleClass().add("empty-label");
            eventsList.getChildren().add(emptyLabel);
            return;
        }

        for (CalendarEvent event : events) {
            VBox eventCard = createEventCard(event);
            eventsList.getChildren().add(eventCard);
        }

        for (Routine routine : routines) {
            VBox routineCard = createRoutineCard(routine);
            eventsList.getChildren().add(routineCard);
        }
    }

    private VBox createEventCard(CalendarEvent event) {
        VBox card = new VBox(8);
        card.getStyleClass().add("exam-card");
        card.setStyle(card.getStyle() + "; -fx-border-color: " + event.getColor() + ";");

        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("exam-title");

        Label typeLabel = new Label(event.getEventType().toString());
        typeLabel.getStyleClass().add("badge");
        typeLabel.setStyle("-fx-background-color: " + event.getColor() + "22; -fx-text-fill: " + event.getColor() + ";");

        if (event.getStartTime() != null) {
            Label timeLabel = new Label("🕐 " + event.getStartTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
            timeLabel.getStyleClass().add("exam-date");
            card.getChildren().addAll(titleLabel, typeLabel, timeLabel);
        } else {
            card.getChildren().addAll(titleLabel, typeLabel);
        }

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            Label locationLabel = new Label("📍 " + event.getLocation());
            locationLabel.setStyle("-fx-text-fill: -text-secondary;");
            card.getChildren().add(locationLabel);
        }

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Label descLabel = new Label(event.getDescription());
            descLabel.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 12;");
            descLabel.setWrapText(true);
            card.getChildren().add(descLabel);
        }

        return card;
    }

    private VBox createRoutineCard(Routine routine) {
        VBox card = new VBox(8);
        card.getStyleClass().add("routine-card");

        Label courseLabel = new Label(routine.getCourseName());
        courseLabel.getStyleClass().add("routine-course-label");

        Label timeLabel = new Label("🕐 " + routine.getTimeStart().format(DateTimeFormatter.ofPattern("hh:mm a")) +
                " - " + routine.getTimeEnd().format(DateTimeFormatter.ofPattern("hh:mm a")));
        timeLabel.getStyleClass().add("routine-time-label");

        card.getChildren().addAll(courseLabel, timeLabel);

        if (routine.getTeacherName() != null && !routine.getTeacherName().isEmpty()) {
            Label teacherLabel = new Label("Teacher: " + routine.getTeacherName());
            teacherLabel.getStyleClass().add("routine-teacher-label");
            card.getChildren().add(teacherLabel);
        }

        if (routine.getRoom() != null && !routine.getRoom().isEmpty()) {
            Label roomLabel = new Label("📍 " + routine.getRoom());
            roomLabel.getStyleClass().add("routine-room-label");
            card.getChildren().add(roomLabel);
        }

        return card;
    }

    private List<CalendarEvent> getEventsForDate(LocalDate date) {
        List<CalendarEvent> allEvents = new ArrayList<>();

        if (studentClassrooms != null) {
            for (Classroom classroom : studentClassrooms) {
                List<CalendarEvent> classroomEvents = CalendarService.getEventsByDate(classroom.getId(), date);
                allEvents.addAll(classroomEvents);
            }
        }

        return allEvents.stream()
                .sorted((e1, e2) -> {
                    if (e1.getStartTime() != null && e2.getStartTime() != null) {
                        return e1.getStartTime().compareTo(e2.getStartTime());
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

    private List<Routine> getRoutinesForDate(LocalDate date) {
        List<Routine> allRoutines = new ArrayList<>();
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        if (studentClassrooms != null) {
            for (Classroom classroom : studentClassrooms) {
                List<Routine> classroomRoutines = RoutineService.getWeeklyRoutine(classroom.getId());
                List<Routine> dayRoutines = classroomRoutines.stream()
                        .filter(r -> r.getDay().equalsIgnoreCase(dayName))
                        .sorted((r1, r2) -> r1.getTimeStart().compareTo(r2.getTimeStart()))
                        .collect(Collectors.toList());
                allRoutines.addAll(dayRoutines);
            }
        }

        return allRoutines;
    }

    private void navigateToView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            System.err.println("Navigation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
