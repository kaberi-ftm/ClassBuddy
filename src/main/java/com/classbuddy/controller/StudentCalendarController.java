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
import com.classbuddy.util.ContextMenuFactory;
import com.classbuddy.util.DateFormats;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
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
    @FXML private Label classroomContextLabel;
    @FXML private Label enrollmentWarningLabel;
    @FXML private Label semesterRangeLabel;

    private User currentStudent;
    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private List<Classroom> studentClassrooms;
    private boolean filterBySemester = true;
    private LocalDate semesterStart;
    private LocalDate semesterEnd;

    @FXML
    public void initialize() {
        currentStudent = LoginController.getCurrentUser();
        
        if (currentStudent != null) {
            studentNameLabel.setText(currentStudent.getUsername());
            studentClassrooms = ClassroomService.getStudentClassrooms(currentStudent.getId());
        }

        computeSemesterRange();
        updateSemesterHeader();
        updateContextHeader();

        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        updateCalendar();
        showEventsForDate(selectedDate);
    }

    public void setSelectedClassroom(Classroom classroom) {
        if (classroom != null) {
            boolean isActive = false;
            if (currentStudent != null) {
                List<Classroom> active = ClassroomService.getStudentClassrooms(currentStudent.getId());
                for (Classroom c : active) {
                    if (c.getId() == classroom.getId()) { isActive = true; break; }
                }
            }

            if (!isActive) {
                if (enrollmentWarningLabel != null) {
                    enrollmentWarningLabel.setText("You are no longer enrolled in this class");
                    enrollmentWarningLabel.setVisible(true);
                    enrollmentWarningLabel.setManaged(true);
                }
                this.studentClassrooms = java.util.List.of();
            } else {
                this.studentClassrooms = java.util.List.of(classroom);
                if (enrollmentWarningLabel != null) {
                    enrollmentWarningLabel.setVisible(false);
                    enrollmentWarningLabel.setManaged(false);
                }
            }
        }

        updateContextHeader(classroom);
        if (calendarGrid != null) {
            currentYearMonth = YearMonth.now();
            selectedDate = LocalDate.now();
            updateCalendar();
            showEventsForDate(selectedDate);
        }
    }

    private void updateContextHeader() {
        updateContextHeader(null);
    }

    private void updateContextHeader(Classroom selected) {
        if (classroomContextLabel == null) return;
        if (studentClassrooms == null || studentClassrooms.isEmpty()) {
            if (selected != null) {
                classroomContextLabel.setText("Class: " + selected.getName() + " (" + selected.getSection() + ", " + selected.getDepartment() + ")");
            } else {
                classroomContextLabel.setText("All Active Classes");
            }
            return;
        }
        if (studentClassrooms.size() == 1) {
            Classroom c = studentClassrooms.get(0);
            classroomContextLabel.setText("Class: " + c.getName() + " (" + c.getSection() + ", " + c.getDepartment() + ")");
        } else {
            classroomContextLabel.setText("All Active Classes");
        }
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

            Scene scene = new Scene(root, 1366, 800);
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
            
            Scene scene = new Scene(root, 1366, 800);
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
        YearMonth prev = currentYearMonth.minusMonths(1);
        if (filterBySemester && semesterStart != null) {
            YearMonth startYm = YearMonth.from(semesterStart);
            currentYearMonth = prev.isBefore(startYm) ? startYm : prev;
        } else {
            currentYearMonth = prev;
        }
        updateCalendar();
    }

    @FXML
    private void handleNextMonth() {
        YearMonth next = currentYearMonth.plusMonths(1);
        if (filterBySemester && semesterEnd != null) {
            YearMonth endYm = YearMonth.from(semesterEnd);
            currentYearMonth = next.isAfter(endYm) ? endYm : next;
        } else {
            currentYearMonth = next;
        }
        updateCalendar();
    }

    @FXML
    private void handleToday() {
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        updateCalendar();
        showEventsForDate(selectedDate);
    }

    @FXML
    private void toggleSemesterFilter() {
        filterBySemester = !filterBySemester;
        updateSemesterHeader();
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
            cell.getStyleClass().add("calendar-day-selected");
        }

        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayNumber);

        boolean outOfRange = filterBySemester && !isWithinSemester(date);

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
            moreLabel.getStyleClass().add("calendar-more-label");
            cell.getChildren().add(moreLabel);
        }

        if (outOfRange) {
            cell.getStyleClass().add("calendar-day-muted");
        }

        // Attach right-click context menu for quick add actions
        ContextMenuFactory.attachStudentMenu(cell, date, studentClassrooms);

        return cell;
    }

    private HBox createEventIndicator(String title, String color) {
        HBox box = new HBox(3);
        box.getStyleClass().addAll("calendar-event", "calendar-event-indicator");
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle("-event-color: " + color + ";");

        Label label = new Label(title.length() > 12 ? title.substring(0, 10) + ".." : title);
        label.getStyleClass().addAll("calendar-event-text", "calendar-event-label");
        box.getChildren().add(label);

        return box;
    }

    private void showEventsForDate(LocalDate date) {
        selectedDateLabel.setText(DateFormats.dateLong(date));
        eventsList.getChildren().clear();

        if (filterBySemester && !isWithinSemester(date)) {
            if (enrollmentWarningLabel != null) {
                enrollmentWarningLabel.setText("Outside semester range");
                enrollmentWarningLabel.setVisible(true);
                enrollmentWarningLabel.setManaged(true);
            }
            return;
        } else if (enrollmentWarningLabel != null &&
                "Outside semester range".equals(enrollmentWarningLabel.getText())) {
            enrollmentWarningLabel.setVisible(false);
            enrollmentWarningLabel.setManaged(false);
        }

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
        card.getStyleClass().addAll("exam-card", "event-card");
        card.setStyle("-event-color: " + event.getColor() + ";");

        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("exam-title");

        Label typeLabel = new Label(event.getEventType().toString());
        typeLabel.getStyleClass().addAll("badge", "badge-tone");
        typeLabel.setStyle("-event-color: " + event.getColor() + ";");

        if (event.getStartTime() != null) {
            Label timeLabel = new Label("🕐 " + DateFormats.time(event.getStartTime()));
            timeLabel.getStyleClass().add("exam-date");
            card.getChildren().addAll(titleLabel, typeLabel, timeLabel);
        } else {
            card.getChildren().addAll(titleLabel, typeLabel);
        }

            if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                Label locationLabel = new Label("📍 " + event.getLocation());
            locationLabel.getStyleClass().add("text-muted");
            card.getChildren().add(locationLabel);
        }

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            Label descLabel = new Label(event.getDescription());
            descLabel.getStyleClass().addAll("text-muted", "body-small");
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

        Label timeLabel = new Label("🕐 " + DateFormats.time(routine.getTimeStart()) +
            " - " + DateFormats.time(routine.getTimeEnd()));
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

        if (filterBySemester && !isWithinSemester(date)) {
            return allEvents;
        }

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

        if (filterBySemester && !isWithinSemester(date)) {
            return allRoutines;
        }

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

    private void computeSemesterRange() {
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        if (month <= 6) {
            semesterStart = LocalDate.of(year, 1, 1);
            semesterEnd = LocalDate.of(year, 6, 30);
        } else {
            semesterStart = LocalDate.of(year, 7, 1);
            semesterEnd = LocalDate.of(year, 12, 31);
        }
    }

    private boolean isWithinSemester(LocalDate date) {
        if (!filterBySemester || semesterStart == null || semesterEnd == null) return true;
        return !(date.isBefore(semesterStart) || date.isAfter(semesterEnd));
    }

    private void updateSemesterHeader() {
        if (semesterRangeLabel == null) return;
        if (!filterBySemester) {
            semesterRangeLabel.setText("All dates");
            return;
        }
        if (semesterStart != null && semesterEnd != null) {
            semesterRangeLabel.setText("Semester: " + DateFormats.dateMed(semesterStart) + " – " + DateFormats.dateMed(semesterEnd));
        }
    }

    private void navigateToView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1366, 800);
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
