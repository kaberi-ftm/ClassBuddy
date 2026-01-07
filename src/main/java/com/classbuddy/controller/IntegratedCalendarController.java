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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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
        // Build a 7-day time grid (08:00–18:00, 30-minute steps)
        GridPane grid = buildTimeGrid(currentWeekStart, 8, 18, 30);

        // Map routines/exams/events into timed blocks and place them
        List<TimedBlock> blocks = getTimedBlocksForWeek(currentWeekStart);
        for (TimedBlock b : blocks) {
            addTimedBlock(grid, b);
        }

        calendarContent.getChildren().add(grid);
        VBox.setVgrow(grid, Priority.ALWAYS);
    }

    

    private void renderDayView() {
        // Build a 1-day time grid centered on currentDay (06:00–22:00, 30-minute steps)
        LocalDate start = currentDay;
        GridPane grid = buildTimeGrid(start, 6, 22, 30);

        // Collect only currentDay blocks
        List<TimedBlock> blocks = getTimedBlocksForDay(currentDay);
        for (TimedBlock b : blocks) {
            addTimedBlock(grid, b);
        }

        calendarContent.getChildren().add(grid);
        VBox.setVgrow(grid, Priority.ALWAYS);
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

    // ==== Time-grid helpers and block mapping ====

    private static class TimedBlock {
        LocalDate date;
        LocalTime start;
        LocalTime end;
        String title;
        String details;
        String cssClass; // e.g., calendar-block-routine / -exam / -event

        int dayIndexFrom(LocalDate weekStart) { return (int) Duration.between(weekStart.atStartOfDay(), date.atStartOfDay()).toDays(); }
    }

    private GridPane buildTimeGrid(LocalDate weekStartOrDay, int startHour, int endHour, int stepMinutes) {
        // Grid columns: 0 = time labels, 1..7 = days (use only 1 for day view)
        GridPane grid = new GridPane();
        grid.getStyleClass().add("calendar-time-grid");
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setStyle("-fx-padding: 10;");

        // Header row
        Label timeHeader = new Label("Time");
        timeHeader.getStyleClass().add("calendar-time-header");
        timeHeader.setMinWidth(80);
        grid.add(timeHeader, 0, 0);

        DateTimeFormatter headerFmt = DateTimeFormatter.ofPattern("EEE M/d");
        int days = currentView == CalendarView.DAY ? 1 : 7;
        for (int d = 0; d < days; d++) {
            LocalDate day = (currentView == CalendarView.DAY ? weekStartOrDay : weekStartOrDay.plusDays(d));
            Label dayLabel = new Label(day.format(headerFmt));
            dayLabel.getStyleClass().add("calendar-time-header");
            if (day.equals(LocalDate.now())) dayLabel.getStyleClass().add("calendar-time-header-today");
            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            grid.add(dayLabel, d + 1, 0);
        }

        // Rows for time slots (30-min increments)
        int rows = ((endHour - startHour) * 60) / stepMinutes;
        for (int r = 0; r < rows; r++) {
            int minuteOfDay = startHour * 60 + r * stepMinutes;
            LocalTime t = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60);

            // Time label at the left only on hour boundaries
            Label tl = new Label(t.getMinute() == 0 ? t.toString() : "");
            tl.getStyleClass().add("calendar-hour-label");
            tl.setMinWidth(80);
            grid.add(tl, 0, r + 1);

            for (int d = 0; d < days; d++) {
                Region cell = new Region();
                cell.getStyleClass().add("calendar-time-cell");
                GridPane.setHgrow(cell, Priority.ALWAYS);
                grid.add(cell, d + 1, r + 1);
            }
        }

        return grid;
    }

    private void addTimedBlock(GridPane grid, TimedBlock b) {
        // Determine base and day index
        LocalDate base = (currentView == CalendarView.DAY) ? currentDay : currentWeekStart;
        int dayIndex = (currentView == CalendarView.DAY) ? 0 : b.dayIndexFrom(base);
        if (dayIndex < 0 || dayIndex > 6) return; // out of displayed range

        // Grid math
        int startHour = (currentView == CalendarView.DAY) ? 6 : 8;
        int step = 30; // minutes
        int startRow = Math.max(0, ((b.start.getHour() * 60 + b.start.getMinute()) - startHour * 60) / step);
        int durationMin = (int) Math.max(30, Duration.between(b.start, b.end).toMinutes());
        int rowSpan = Math.max(1, (int) Math.ceil(durationMin / (double) step));

        // Create the visual block
        VBox block = new VBox(2);
        block.getStyleClass().addAll("calendar-block", b.cssClass);
        Label title = new Label(b.title);
        title.getStyleClass().add("calendar-block-title");
        Label subtitle = new Label(b.details);
        subtitle.getStyleClass().add("calendar-block-subtitle");
        subtitle.setWrapText(true);
        block.getChildren().addAll(title, subtitle);

        // Place in grid (row offset +1 due to header row)
        grid.add(block, dayIndex + 1, startRow + 1, 1, rowSpan);
    }

    private List<TimedBlock> getTimedBlocksForWeek(LocalDate weekStart) {
        List<TimedBlock> blocks = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            blocks.addAll(getTimedBlocksForDay(day));
        }
        return blocks;
    }

    private List<TimedBlock> getTimedBlocksForDay(LocalDate day) {
        List<TimedBlock> list = new ArrayList<>();

        // Routines (recurring by weekday)
        String dayName = day.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        if (classroomRoutines != null) {
            for (Routine r : classroomRoutines) {
                if (r.getDay().equalsIgnoreCase(dayName) && r.getTimeStart() != null && r.getTimeEnd() != null) {
                    TimedBlock b = new TimedBlock();
                    b.date = day;
                    b.start = r.getTimeStart();
                    b.end = r.getTimeEnd();
                    b.title = r.getCourseName();
                    b.details = joinNonEmpty(
                        r.getTeacherName(),
                        (r.getRoom() != null && !r.getRoom().isEmpty() ? "Room " + r.getRoom() : null)
                    );
                    b.cssClass = "calendar-block-routine";
                    list.add(b);
                }
            }
        }

        // Exams (assume 60 min when end not specified)
        if (classroomExams != null) {
            for (Exam e : classroomExams) {
                if (day.equals(e.getExamDate()) && e.getExamTime() != null) {
                    TimedBlock b = new TimedBlock();
                    b.date = day;
                    b.start = e.getExamTime();
                    b.end = e.getExamTime().plusMinutes(60);
                    b.title = e.getCourseName() + " (" + e.getExamType() + ")";
                    b.details = (e.getRoom() != null ? "Room " + e.getRoom() : "");
                    b.cssClass = "calendar-block-exam";
                    list.add(b);
                }
            }
        }

        // Calendar events (use end if provided; else 60 min)
        if (calendarEvents != null) {
            for (CalendarEvent ce : calendarEvents) {
                if (day.equals(ce.getEventDate()) && ce.getStartTime() != null) {
                    TimedBlock b = new TimedBlock();
                    b.date = day;
                    b.start = ce.getStartTime();
                    b.end = (ce.getEndTime() != null) ? ce.getEndTime() : ce.getStartTime().plusMinutes(60);
                    b.title = ce.getTitle();
                    b.details = joinNonEmpty(ce.getLocation(), ce.getDescription());
                    b.cssClass = "calendar-block-event";
                    list.add(b);
                }
            }
        }

        return list;
    }

    private String joinNonEmpty(String... parts) {
        return Arrays.stream(parts)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" · "));
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
