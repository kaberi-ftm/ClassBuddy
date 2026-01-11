package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.model.Routine;
import com.classbuddy.model.CalendarEvent;
import com.classbuddy.service.RoutineService;
import com.classbuddy.service.CalendarService;
import com.classbuddy.util.DateFormats;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CalendarViewController - Enhanced with time-slotted view support
 */
public class CalendarViewController {
    
    @FXML
    private Label monthYearLabel;
    
    @FXML
    private GridPane calendarGrid;
    
    @FXML
    private Button prevMonthBtn;
    
    @FXML
    private Button nextMonthBtn;
    
    private YearMonth currentYearMonth;
    private Classroom classroom;
    private List<Routine> routines;
    private List<CalendarEvent> calendarEvents;
    
    public void initialize() {
        currentYearMonth = YearMonth.now();
        updateCalendar();
    }
    
    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroom != null) {
            loadRoutines();
            loadCalendarEvents();
        }
    }
    
    private void loadRoutines() {
        if (classroom != null) {
            routines = RoutineService.getWeeklyRoutine(classroom.getId());
            updateCalendar();
        }
    }
    
    private void loadCalendarEvents() {
        if (classroom != null) {
            // Load events for the current month
            LocalDate firstDay = currentYearMonth.atDay(1);
            LocalDate lastDay = currentYearMonth.atEndOfMonth();
            calendarEvents = CalendarService.getEventsByDateRange(
                classroom.getId(), 
                firstDay.minusDays(7),  // Include previous week
                lastDay.plusDays(7)     // Include next week
            );
            updateCalendar();
        }
    }
    
    @FXML
    private void handlePrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        loadCalendarEvents();
    }
    
    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        loadCalendarEvents();
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
        VBox cell = new VBox(3);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.getStyleClass().add("calendar-day");
        cell.setMinHeight(80);
        cell.setMaxHeight(120);
        
        if (date.getMonth() != currentYearMonth.getMonth()) {
            cell.getStyleClass().add("calendar-day-other-month");
        }
        
        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("calendar-day-today");
        }
        
        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayNumber);
        
        // Get calendar events for this date
        LocalDate cellDate = date;
        List<CalendarEvent> dayEvents = calendarEvents != null ? 
            calendarEvents.stream()
                .filter(e -> e.getEventDate().equals(cellDate))
                .sorted((e1, e2) -> {
                    if (e1.getStartTime() != null && e2.getStartTime() != null) {
                        return e1.getStartTime().compareTo(e2.getStartTime());
                    }
                    return 0;
                })
                .collect(Collectors.toList()) : List.of();
        
        // Also check for routine-based events
        if (routines != null && !routines.isEmpty()) {
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            List<Routine> dayRoutines = routines.stream()
                    .filter(r -> r.getDay().equalsIgnoreCase(dayName))
                    .sorted((r1, r2) -> r1.getTimeStart().compareTo(r2.getTimeStart()))
                    .collect(Collectors.toList());
            
            // Display up to 2 events/routines
            int displayCount = 0;
            int maxDisplay = 2;
            
            for (CalendarEvent event : dayEvents) {
                if (displayCount >= maxDisplay) break;
                
                HBox eventBox = createEventBox(event);
                cell.getChildren().add(eventBox);
                displayCount++;
            }
            
            for (Routine routine : dayRoutines) {
                if (displayCount >= maxDisplay) break;
                
                HBox eventBox = createRoutineBox(routine);
                cell.getChildren().add(eventBox);
                displayCount++;
            }
            
            int totalItems = dayEvents.size() + dayRoutines.size();
            if (totalItems > maxDisplay) {
                Label moreLabel = new Label("+" + (totalItems - maxDisplay) + " more");
                moreLabel.getStyleClass().add("calendar-more-label");
                cell.getChildren().add(moreLabel);
            }
        } else {
            // Only show calendar events
            int displayCount = 0;
            for (CalendarEvent event : dayEvents) {
                if (displayCount >= 2) break;
                
                HBox eventBox = createEventBox(event);
                cell.getChildren().add(eventBox);
                displayCount++;
            }
            
            if (dayEvents.size() > 2) {
                Label moreLabel = new Label("+" + (dayEvents.size() - 2) + " more");
                moreLabel.getStyleClass().add("calendar-more-label");
                cell.getChildren().add(moreLabel);
            }
        }
        
        return cell;
    }
    
    /**
     * Create event box for calendar event
     */
    private HBox createEventBox(CalendarEvent event) {
        HBox eventBox = new HBox(3);
        eventBox.getStyleClass().addAll("calendar-event", "calendar-event-indicator");
        eventBox.setMaxWidth(Double.MAX_VALUE);
        eventBox.setStyle("-event-color: " + event.getColor() + ";");
        
        // Time indicator (optional)
        if (event.getStartTime() != null) {
            Label timeLabel = new Label(DateFormats.time(event.getStartTime()));
            timeLabel.getStyleClass().addAll("body-xs", "text-muted");
            eventBox.getChildren().add(timeLabel);
        }
        
        Label eventLabel = new Label(event.getTitle());
        eventLabel.getStyleClass().addAll("calendar-event-text", "calendar-event-label");
        eventLabel.setMaxWidth(Double.MAX_VALUE);
        eventLabel.setWrapText(true);
        eventBox.getChildren().add(eventLabel);
        
        // Context menu: delete calendar event (admin-only)
        boolean isAdmin = LoginController.getCurrentUser() != null && LoginController.getCurrentUser().getRole().name().equals("ADMIN");
        if (isAdmin) {
            eventBox.addEventFilter(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
                ContextMenu menu = new ContextMenu();
                MenuItem del = new MenuItem("Delete Event");
                del.setOnAction(ev -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Event");
                    confirm.setHeaderText("Delete this event?");
                    confirm.setContentText(event.getTitle());
                    Optional<ButtonType> res = confirm.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.OK) {
                        if (CalendarService.deleteEvent(event.getId())) {
                            loadCalendarEvents();
                        }
                    }
                });
                menu.getItems().add(del);
                menu.show(eventBox, e.getScreenX(), e.getScreenY());
                e.consume();
            });
        }
        
        return eventBox;
    }
    
    /**
     * Create event box for routine
     */
    private HBox createRoutineBox(Routine routine) {
        HBox eventBox = new HBox(3);
        eventBox.getStyleClass().addAll("calendar-event", "calendar-event-indicator");
        eventBox.setMaxWidth(Double.MAX_VALUE);
        eventBox.setStyle("-event-color: -primary-orange;");
        
        Label timeLabel = new Label(DateFormats.time(routine.getTimeStart()));
        timeLabel.getStyleClass().addAll("body-xs", "text-muted");
        eventBox.getChildren().add(timeLabel);
        
        Label eventLabel = new Label(routine.getCourseName());
        eventLabel.getStyleClass().add("calendar-event-text");
        eventLabel.setMaxWidth(Double.MAX_VALUE);
        eventLabel.setWrapText(true);
        eventBox.getChildren().add(eventLabel);
        
        // Context menu: edit / delete routine occurrence (admin-only)
        boolean isAdminRoutine = LoginController.getCurrentUser() != null && LoginController.getCurrentUser().getRole().name().equals("ADMIN");
        if (isAdminRoutine) {
            eventBox.addEventFilter(javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
                ContextMenu menu = new ContextMenu();
                MenuItem edit = new MenuItem("Edit Routine");
                edit.setOnAction(ev -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-routine.fxml"));
                        Parent root = loader.load();
                        EditRoutineController ctrl = loader.getController();
                        ctrl.setClassroom(classroom);
                        ctrl.setRoutine(routine);
                        ctrl.loadData();
                        Scene scene = new Scene(root, 1366, 800);
                        Stage stage = (Stage) eventBox.getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();
                        com.classbuddy.util.ViewTransitions.fadeIn(root);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                MenuItem del = new MenuItem("Delete Routine");
                del.setOnAction(ev -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Routine");
                    confirm.setHeaderText("Delete this routine occurrence?");
                    confirm.setContentText(routine.toString());
                    Optional<ButtonType> res = confirm.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.OK) {
                        if (RoutineService.deleteRoutine(routine.getId())) {
                            loadRoutines();
                            loadCalendarEvents();
                        }
                    }
                });
                menu.getItems().addAll(edit, del);
                menu.show(eventBox, e.getScreenX(), e.getScreenY());
                e.consume();
            });
        }
        
        return eventBox;
    }
    
    public void refresh() {
        if (classroom != null) {
            loadRoutines();
            loadCalendarEvents();
        }
    }
}
