package com.classbuddy.controller;

import com.classbuddy.model.CalendarEvent;
import com.classbuddy.model.Classroom;
import com.classbuddy.model.Exam;
import com.classbuddy.model.Routine;
import com.classbuddy.service.CalendarService;
import com.classbuddy.service.ExamService;
import com.classbuddy.service.RoutineService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Integrated Calendar Controller - Month/Week/Day views for classroom
 */
public class IntegratedCalendarController {

    @FXML private HBox viewSwitcher;
    @FXML private Button monthViewBtn;
    @FXML private Button weekViewBtn;
    @FXML private Button dayViewBtn;
    @FXML private Label calendarTitle;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Button todayBtn;
    @FXML private VBox calendarContent;
    @FXML private VBox eventDetailsPanel;
    @FXML private Label selectedDateLabel;
    @FXML private VBox eventsList;

    private Classroom classroom;
    private CalendarView currentView = CalendarView.MONTH;
    private YearMonth currentYearMonth;
    private LocalDate currentWeekStart;
    private LocalDate currentDay;
    private LocalDate selectedDate;

    private List<Routine> classroomRoutines;
    private List<Exam> classroomExams;
    private List<CalendarEvent> calendarEvents;

    private enum CalendarView {
        MONTH, WEEK, DAY
    }

    public void initialize() {
        currentYearMonth = YearMonth.now();
        currentWeekStart = getWeekStart(LocalDate.now());
        currentDay = LocalDate.now();
        selectedDate = LocalDate.now();
        
        setupViewSwitcher();
        updateView();
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        loadData();
        updateView();
    }

    private void loadData() {
        if (classroom == null) return;

        classroomRoutines = RoutineService.getWeeklyRoutine(classroom.getId());
        classroomExams = ExamService.getClassroomExams(classroom.getId());
        
        LocalDate start = getCurrentRangeStart();
        LocalDate end = getCurrentRangeEnd();
        calendarEvents = CalendarService.getEventsByDateRange(classroom.getId(), start, end);
    }

    private void setupViewSwitcher() {
        monthViewBtn.setOnAction(e -> switchView(CalendarView.MONTH));
        weekViewBtn.setOnAction(e -> switchView(CalendarView.WEEK));
        dayViewBtn.setOnAction(e -> switchView(CalendarView.DAY));
        
        prevBtn.setOnAction(e -> navigatePrevious());
        nextBtn.setOnAction(e -> navigateNext());
        todayBtn.setOnAction(e -> navigateToday());
    }

    private void switchView(CalendarView view) {
        currentView = view;
        
        // Update button styles
        monthViewBtn.getStyleClass().remove("btn-primary");
        weekViewBtn.getStyleClass().remove("btn-primary");
        dayViewBtn.getStyleClass().remove("btn-primary");
        
        monthViewBtn.getStyleClass().add("btn-ghost");
        weekViewBtn.getStyleClass().add("btn-ghost");
        dayViewBtn.getStyleClass().add("btn-ghost");
        
        switch (view) {
            case MONTH:
                monthViewBtn.getStyleClass().remove("btn-ghost");
                monthViewBtn.getStyleClass().add("btn-primary");
                break;
            case WEEK:
                weekViewBtn.getStyleClass().remove("btn-ghost");
                weekViewBtn.getStyleClass().add("btn-primary");
                break;
            case DAY:
                dayViewBtn.getStyleClass().remove("btn-ghost");
                dayViewBtn.getStyleClass().add("btn-primary");
                break;
        }
        
        updateView();
    }

    private void navigatePrevious() {
        switch (currentView) {
            case MONTH:
                currentYearMonth = currentYearMonth.minusMonths(1);
                break;
            case WEEK:
                currentWeekStart = currentWeekStart.minusWeeks(1);
                break;
            case DAY:
                currentDay = currentDay.minusDays(1);
                break;
        }
        loadData();
        updateView();
    }

    private void navigateNext() {
        switch (currentView) {
            case MONTH:
                currentYearMonth = currentYearMonth.plusMonths(1);
                break;
            case WEEK:
                currentWeekStart = currentWeekStart.plusWeeks(1);
                break;
            case DAY:
                currentDay = currentDay.plusDays(1);
                break;
        }
        loadData();
        updateView();
    }

    private void navigateToday() {
        currentYearMonth = YearMonth.now();
        currentWeekStart = getWeekStart(LocalDate.now());
        currentDay = LocalDate.now();
        selectedDate = LocalDate.now();
        loadData();
        updateView();
    }

    private void updateView() {
        updateTitle();
        calendarContent.getChildren().clear();
        
        switch (currentView) {
            case MONTH:
                renderMonthView();
                break;
            case WEEK:
                renderWeekView();
                break;
            case DAY:
                renderDayView();
                break;
        }
        
        updateEventDetails();
    }

    private void updateTitle() {
        switch (currentView) {
            case MONTH:
                String monthName = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                calendarTitle.setText(monthName + " " + currentYearMonth.getYear());
                break;
            case WEEK:
                LocalDate weekEnd = currentWeekStart.plusDays(6);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
                calendarTitle.setText(currentWeekStart.format(formatter) + " - " + weekEnd.format(formatter) + ", " + currentWeekStart.getYear());
                break;
            case DAY:
                DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
                calendarTitle.setText(currentDay.format(dayFormatter));
                break;
        }
    }

    private void renderMonthView() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setStyle("-fx-padding: 15;");
        
        // Day headers
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.getStyleClass().add("calendar-day-header");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            grid.add(dayLabel, i, 0);
        }
        
        // Calendar days
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        LocalDate date = firstOfMonth.minusDays(dayOfWeek);
        
        int row = 1;
        int col = 0;
        
        while (date.isBefore(currentYearMonth.atEndOfMonth().plusDays(1)) || col != 0) {
            LocalDate cellDate = date;
            VBox dayCell = createMonthDayCell(cellDate);
            GridPane.setHgrow(dayCell, Priority.ALWAYS);
            GridPane.setVgrow(dayCell, Priority.ALWAYS);
            grid.add(dayCell, col, row);
            
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
            date = date.plusDays(1);
        }
        
        calendarContent.getChildren().add(grid);
        VBox.setVgrow(grid, Priority.ALWAYS);
    }

    private VBox createMonthDayCell(LocalDate date) {
        VBox cell = new VBox(3);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.getStyleClass().add("calendar-day");
        cell.setMinHeight(90);
        cell.setMaxHeight(110);
        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            updateView(); // Refresh selection + details (prevents duplicate month grids)
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

        // Add event indicators
        List<Object> dayEvents = getEventsForDate(date);
        int displayCount = 0;
        for (Object event : dayEvents) {
            if (displayCount >= 3) break;
            HBox indicator = createEventIndicator(event);
            cell.getChildren().add(indicator);
            displayCount++;
        }

        if (dayEvents.size() > 3) {
            Label moreLabel = new Label("+" + (dayEvents.size() - 3) + " more");
            moreLabel.setStyle("-fx-text-fill: -text-light; -fx-font-size: 9;");
            cell.getChildren().add(moreLabel);
        }

        return cell;
    }

    private void renderWeekView() {
        VBox weekContainer = new VBox(10);
        weekContainer.setStyle("-fx-padding: 15;");
        
        // Header row with days
        HBox headerRow = new HBox(5);
        headerRow.setStyle("-fx-padding: 0 0 10 0;");
        
        Label timeLabel = new Label("Time");
        timeLabel.setMinWidth(80);
        timeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-primary;");
        headerRow.getChildren().add(timeLabel);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE M/d");
        for (int i = 0; i < 7; i++) {
            LocalDate day = currentWeekStart.plusDays(i);
            Label dayLabel = new Label(day.format(formatter));
            dayLabel.setMinWidth(120);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-primary; -fx-alignment: center;");
            if (day.equals(LocalDate.now())) {
                dayLabel.setStyle(dayLabel.getStyle() + "; -fx-text-fill: -primary-orange;");
            }
            HBox.setHgrow(dayLabel, Priority.ALWAYS);
            headerRow.getChildren().add(dayLabel);
        }
        
        weekContainer.getChildren().add(headerRow);
        
        // Time slots (8 AM to 6 PM)
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox timeSlots = new VBox(2);
        for (int hour = 8; hour <= 18; hour++) {
            HBox timeSlot = createWeekTimeSlot(hour);
            timeSlots.getChildren().add(timeSlot);
        }
        
        scrollPane.setContent(timeSlots);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        weekContainer.getChildren().add(scrollPane);
        
        calendarContent.getChildren().add(weekContainer);
        VBox.setVgrow(weekContainer, Priority.ALWAYS);
    }

    private HBox createWeekTimeSlot(int hour) {
        HBox slot = new HBox(5);
        slot.setMinHeight(60);
        slot.setStyle("-fx-border-color: -border-color; -fx-border-width: 0 0 1 0;");
        
        Label timeLabel = new Label(String.format("%02d:00", hour));
        timeLabel.setMinWidth(80);
        timeLabel.setStyle("-fx-text-fill: -text-light; -fx-font-size: 12;");
        slot.getChildren().add(timeLabel);
        
        for (int day = 0; day < 7; day++) {
            LocalDate date = currentWeekStart.plusDays(day);
            VBox daySlot = new VBox(3);
            daySlot.setMinWidth(120);
            daySlot.setStyle("-fx-border-color: -border-color; -fx-border-width: 0 0 0 1; -fx-padding: 5;");
            HBox.setHgrow(daySlot, Priority.ALWAYS);
            
            // Find events for this time slot
            List<Object> events = getEventsForDateTime(date, hour);
            for (Object event : events) {
                Label eventLabel = createEventLabel(event);
                daySlot.getChildren().add(eventLabel);
            }
            
            slot.getChildren().add(daySlot);
        }
        
        return slot;
    }

    private void renderDayView() {
        VBox dayContainer = new VBox(10);
        dayContainer.setStyle("-fx-padding: 15;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        VBox timeSlots = new VBox(2);
        for (int hour = 6; hour <= 22; hour++) {
            HBox timeSlot = createDayTimeSlot(hour);
            timeSlots.getChildren().add(timeSlot);
        }
        
        scrollPane.setContent(timeSlots);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        dayContainer.getChildren().add(scrollPane);
        
        calendarContent.getChildren().add(dayContainer);
        VBox.setVgrow(dayContainer, Priority.ALWAYS);
    }

    private HBox createDayTimeSlot(int hour) {
        HBox slot = new HBox(15);
        slot.setMinHeight(80);
        slot.setStyle("-fx-border-color: -border-color; -fx-border-width: 0 0 1 0; -fx-padding: 10;");
        
        Label timeLabel = new Label(String.format("%02d:00", hour));
        timeLabel.setMinWidth(80);
        timeLabel.setStyle("-fx-text-fill: -text-primary; -fx-font-size: 14; -fx-font-weight: bold;");
        slot.getChildren().add(timeLabel);
        
        VBox eventsBox = new VBox(5);
        HBox.setHgrow(eventsBox, Priority.ALWAYS);
        
        List<Object> events = getEventsForDateTime(currentDay, hour);
        if (events.isEmpty()) {
            Label emptyLabel = new Label("No events");
            emptyLabel.setStyle("-fx-text-fill: -text-light; -fx-font-size: 12; -fx-font-style: italic;");
            eventsBox.getChildren().add(emptyLabel);
        } else {
            for (Object event : events) {
                VBox eventCard = createEventCard(event);
                eventsBox.getChildren().add(eventCard);
            }
        }
        
        slot.getChildren().add(eventsBox);
        return slot;
    }

    private List<Object> getEventsForDate(LocalDate date) {
        List<Object> allEvents = new ArrayList<>();
        
        // Add routines for this day
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        if (classroomRoutines != null) {
            allEvents.addAll(classroomRoutines.stream()
                .filter(r -> r.getDay().equalsIgnoreCase(dayName))
                .collect(Collectors.toList()));
        }
        
        // Add exams
        if (classroomExams != null) {
            allEvents.addAll(classroomExams.stream()
                .filter(e -> e.getExamDate().equals(date))
                .collect(Collectors.toList()));
        }
        
        // Add calendar events
        if (calendarEvents != null) {
            allEvents.addAll(calendarEvents.stream()
                .filter(e -> e.getEventDate().equals(date))
                .collect(Collectors.toList()));
        }
        
        return allEvents;
    }

    private List<Object> getEventsForDateTime(LocalDate date, int hour) {
        List<Object> events = getEventsForDate(date);
        return events.stream()
            .filter(event -> {
                LocalTime eventTime = getEventTime(event);
                return eventTime != null && eventTime.getHour() == hour;
            })
            .collect(Collectors.toList());
    }

    private LocalTime getEventTime(Object event) {
        if (event instanceof Routine) {
            return ((Routine) event).getTimeStart();
        } else if (event instanceof Exam) {
            return ((Exam) event).getExamTime();
        } else if (event instanceof CalendarEvent) {
            return ((CalendarEvent) event).getStartTime();
        }
        return null;
    }

    private HBox createEventIndicator(Object event) {
        HBox indicator = new HBox();
        indicator.setMaxWidth(Double.MAX_VALUE);
        indicator.setStyle("-fx-background-color: " + getEventColor(event) + "22; " +
                          "-fx-border-color: " + getEventColor(event) + "; " +
                          "-fx-border-width: 0 0 0 3; -fx-padding: 2 5; " +
                          "-fx-background-radius: 3;");
        
        Label label = new Label(getEventTitle(event));
        label.setStyle("-fx-text-fill: -text-primary; -fx-font-size: 9;");
        indicator.getChildren().add(label);
        
        return indicator;
    }

    private Label createEventLabel(Object event) {
        Label label = new Label(getEventTitle(event));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
        label.setStyle("-fx-background-color: " + getEventColor(event) + "22; " +
                      "-fx-border-color: " + getEventColor(event) + "; " +
                      "-fx-border-width: 0 0 0 3; -fx-padding: 5 8; " +
                      "-fx-text-fill: -text-primary; -fx-font-size: 11; " +
                      "-fx-background-radius: 4;");
        return label;
    }

    private VBox createEventCard(Object event) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setStyle(card.getStyle() + "; -fx-border-color: " + getEventColor(event) + ";");
        
        Label titleLabel = new Label(getEventTitle(event));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -text-primary;");
        
        Label detailsLabel = new Label(getEventDetails(event));
        detailsLabel.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 12;");
        detailsLabel.setWrapText(true);
        
        card.getChildren().addAll(titleLabel, detailsLabel);
        return card;
    }

    private String getEventTitle(Object event) {
        if (event instanceof Routine) {
            return ((Routine) event).getCourseName();
        } else if (event instanceof Exam) {
            return ((Exam) event).getCourseName() + " Exam";
        } else if (event instanceof CalendarEvent) {
            return ((CalendarEvent) event).getTitle();
        }
        return "Event";
    }

    private String getEventDetails(Object event) {
        if (event instanceof Routine) {
            Routine r = (Routine) event;
            return r.getTimeStart() + " - " + r.getTimeEnd() + 
                   (r.getRoom() != null ? " | Room: " + r.getRoom() : "") +
                   (r.getTeacherName() != null ? " | " + r.getTeacherName() : "");
        } else if (event instanceof Exam) {
            Exam e = (Exam) event;
            return e.getExamTime() + " | " + e.getExamType() +
                   (e.getRoom() != null ? " | Room: " + e.getRoom() : "");
        } else if (event instanceof CalendarEvent) {
            CalendarEvent ce = (CalendarEvent) event;
            return (ce.getStartTime() != null ? ce.getStartTime().toString() : "") +
                   (ce.getLocation() != null ? " | " + ce.getLocation() : "");
        }
        return "";
    }

    private String getEventColor(Object event) {
        if (event instanceof Routine) return "#6366F1"; // Indigo
        if (event instanceof Exam) return "#ef4444"; // Red
        if (event instanceof CalendarEvent) return "#06B6D4"; // Cyan
        return "#6B7A90"; // Neutral
    }

    private void updateEventDetails() {
        if (selectedDate == null) selectedDate = LocalDate.now();
        
        selectedDateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        eventsList.getChildren().clear();
        
        List<Object> events = getEventsForDate(selectedDate);
        
        if (events.isEmpty()) {
            Label emptyLabel = new Label("No events scheduled for this day");
            emptyLabel.getStyleClass().add("empty-label");
            eventsList.getChildren().add(emptyLabel);
        } else {
            for (Object event : events) {
                VBox eventCard = createDetailEventCard(event);
                eventsList.getChildren().add(eventCard);
            }
        }
    }

    private VBox createDetailEventCard(Object event) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setStyle(card.getStyle() + "; -fx-border-color: " + getEventColor(event) + ";");
        
        Label titleLabel = new Label(getEventTitle(event));
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: -text-primary;");
        
        Label typeLabel = new Label(getEventType(event));
        typeLabel.getStyleClass().add("badge");
        typeLabel.setStyle("-fx-background-color: " + getEventColor(event) + "22; " +
                          "-fx-text-fill: " + getEventColor(event) + ";");
        
        Label detailsLabel = new Label(getEventDetails(event));
        detailsLabel.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 13;");
        detailsLabel.setWrapText(true);
        
        card.getChildren().addAll(titleLabel, typeLabel, detailsLabel);
        return card;
    }

    private String getEventType(Object event) {
        if (event instanceof Routine) return "Class";
        if (event instanceof Exam) return "Exam";
        if (event instanceof CalendarEvent) return "Event";
        return "Activity";
    }

    private LocalDate getCurrentRangeStart() {
        switch (currentView) {
            case MONTH:
                return currentYearMonth.atDay(1).minusWeeks(1);
            case WEEK:
                return currentWeekStart;
            case DAY:
                return currentDay;
        }
        return LocalDate.now();
    }

    private LocalDate getCurrentRangeEnd() {
        switch (currentView) {
            case MONTH:
                return currentYearMonth.atEndOfMonth().plusWeeks(1);
            case WEEK:
                return currentWeekStart.plusWeeks(1);
            case DAY:
                return currentDay.plusDays(1);
        }
        return LocalDate.now();
    }

    private LocalDate getWeekStart(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() % 7);
    }

    public void refresh() {
        loadData();
        updateView();
    }
}
