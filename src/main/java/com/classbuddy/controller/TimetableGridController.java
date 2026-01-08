package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.model.Routine;
import com.classbuddy.util.TimetableSlots;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableGridController {

    private static final List<String> DAY_ORDER = List.of(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    );

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private GridPane grid;

    private Classroom classroom;

    private static ContextMenu activeMenu;

    private List<Routine> lastRoutines = List.of();

    @FXML
    private void initialize() {
        if (scrollPane == null) return;

        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            // Horizontal scroll with mouse wheel.
            // Keep Shift+wheel available for vertical scrolling.
            if (e.isShiftDown()) return;
            if (scrollPane.getContent() == null) return;

            double contentWidth = scrollPane.getContent().getBoundsInLocal().getWidth();
            double viewportWidth = scrollPane.getViewportBounds().getWidth();
            double max = contentWidth - viewportWidth;
            if (max <= 0) return;

            double deltaY = e.getDeltaY();
            if (deltaY == 0) return;

            double current = scrollPane.getHvalue();
            double step = (-deltaY / max) * 2.0;
            scrollPane.setHvalue(clamp01(current + step));
            e.consume();
        });
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public void setRoutines(List<Routine> routines) {
        this.lastRoutines = routines == null ? List.of() : routines;
        render(routines);
    }

    public void clear() {
        if (grid != null) {
            grid.getChildren().clear();
            grid.getColumnConstraints().clear();
            grid.getRowConstraints().clear();
        }
    }

    private void render(List<Routine> routines) {
        if (grid == null) return;

        clear();

        List<Routine> safe = routines == null ? List.of() : routines;

        // Use the canonical default slots for a consistent timetable layout.
        // Spanning is calculated by time overlap against these slots.
        List<TimetableSlots.Slot> slots = TimetableSlots.defaultSlots();

        // Index routines by day (for span calculation)
        Map<String, List<Routine>> routinesByDay = new HashMap<>();
        for (Routine r : safe) {
            String day = r.getDay();
            if (day == null) continue;
            routinesByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(r);
        }

        // Column sizing: 0 = day labels; 1..N = slots
        ColumnConstraints dayCol = new ColumnConstraints();
        dayCol.setHgrow(Priority.NEVER);
        dayCol.setMinWidth(140);
        grid.getColumnConstraints().add(dayCol);

        for (int i = 0; i < slots.size(); i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setMinWidth(160);
            grid.getColumnConstraints().add(col);
        }

        // Header row
        grid.add(makeHeaderCell("Day"), 0, 0);
        for (int c = 0; c < slots.size(); c++) {
            grid.add(makeHeaderCell(slots.get(c).label()), c + 1, 0);
        }

        // Day rows
        int row = 1;
        for (String day : DAY_ORDER) {
            grid.add(makeDayCell(day), 0, row);

            List<Routine> dayRoutines = routinesByDay.getOrDefault(day, List.of());
            Placement placement = computePlacement(slots, dayRoutines);

            int c = 0;
            while (c < slots.size()) {
                TimetableSlots.Slot slot = slots.get(c);

                Routine startRoutine = placement.startRoutineBySlotIndex.get(c);
                Integer span = placement.spanByStartSlotIndex.get(c);

                if (startRoutine != null && span != null && span > 0) {
                    VBox cell = makeRoutineCell(startRoutine, slot);
                    GridPane.setColumnSpan(cell, span);
                    grid.add(cell, c + 1, row);
                    c += span;
                    continue;
                }

                if (placement.occupied[c]) {
                    // Covered by a spanning routine starting earlier.
                    c++;
                    continue;
                }

                if (TimetableSlots.isBreak(classroom != null ? classroom.getId() : 0, slot)) {
                    grid.add(makeBreakCell(day, slot), c + 1, row);
                } else {
                    grid.add(makeEmptyCell(day, slot), c + 1, row);
                }
                c++;
            }

            row++;
        }
    }

    private VBox makeHeaderCell(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("timetable-header-text");

        VBox box = new VBox(label);
        box.getStyleClass().add("timetable-header");
        return box;
    }

    private VBox makeDayCell(String day) {
        Label label = new Label(day);
        label.getStyleClass().add("timetable-day-text");

        VBox box = new VBox(label);
        box.getStyleClass().add("timetable-day");
        return box;
    }

    private VBox makeEmptyCell(String day, TimetableSlots.Slot slot) {
        VBox box = new VBox();
        box.getStyleClass().addAll("timetable-cell", "timetable-empty");

        box.setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            openAddRoutine(day, slot);
            e.consume();
        });

        box.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            ContextMenu menu = new ContextMenu();
            MenuItem add = new MenuItem("Add Routine");
            add.setOnAction(ev -> openAddRoutine(day, slot));
            menu.getItems().add(add);

            if (classroom != null) {
                boolean isBreak = TimetableSlots.isBreak(classroom.getId(), slot);
                MenuItem toggleBreak = new MenuItem(isBreak ? "Unset Break" : "Set as Break");
                toggleBreak.setOnAction(ev -> {
                    TimetableSlots.setBreak(classroom.getId(), slot, !isBreak);
                    render(lastRoutines);
                });
                menu.getItems().add(toggleBreak);
            }

            showOnlyOne(menu, box, e.getScreenX(), e.getScreenY());
            e.consume();
        });

        return box;
    }

    private VBox makeBreakCell(String day, TimetableSlots.Slot slot) {
        Label label = new Label("BREAK");
        label.getStyleClass().add("timetable-break-text");

        VBox box = new VBox(label);
        box.getStyleClass().addAll("timetable-cell", "timetable-break");

        box.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            ContextMenu menu = new ContextMenu();

            if (classroom != null) {
                MenuItem unset = new MenuItem("Unset Break");
                unset.setOnAction(ev -> {
                    TimetableSlots.setBreak(classroom.getId(), slot, false);
                    render(lastRoutines);
                });
                menu.getItems().add(unset);
            }

            MenuItem add = new MenuItem("Add Routine");
            add.setOnAction(ev -> openAddRoutine(day, slot));
            menu.getItems().add(add);

            showOnlyOne(menu, box, e.getScreenX(), e.getScreenY());
            e.consume();
        });

        return box;
    }

    private VBox makeRoutineCell(Routine r, TimetableSlots.Slot slot) {
        String title = r.getCourseName() != null ? r.getCourseName() : "(No title)";

        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().add("timetable-entry-title");

        StringBuilder sub = new StringBuilder();
        if (r.getTeacherName() != null && !r.getTeacherName().isBlank()) {
            sub.append(r.getTeacherName().trim());
        }
        if (r.getRoom() != null && !r.getRoom().isBlank()) {
            if (!sub.isEmpty()) sub.append(" · ");
            sub.append("Room ").append(r.getRoom().trim());
        }

        Label subLabel = null;
        if (!sub.isEmpty()) {
            subLabel = new Label(sub.toString());
            subLabel.setWrapText(true);
            subLabel.getStyleClass().add("timetable-entry-sub");
        }

        VBox box = new VBox(6);
        box.getStyleClass().addAll("timetable-cell", "timetable-entry");

        // Simple "color coding" using existing semantic tokens (no new colors):
        // treat long blocks / labs differently.
        if (slot.minutes() >= 120 || title.toLowerCase().contains("lab")) {
            box.getStyleClass().add("timetable-entry-lab");
        }

        box.getChildren().add(titleLabel);
        if (subLabel != null) box.getChildren().add(subLabel);

        box.setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            openEditRoutine(r);
            e.consume();
        });

        box.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            ContextMenu menu = new ContextMenu();
            MenuItem edit = new MenuItem("Edit Routine");
            edit.setOnAction(ev -> openEditRoutine(r));
            menu.getItems().add(edit);
            showOnlyOne(menu, box, e.getScreenX(), e.getScreenY());
            e.consume();
        });

        return box;
    }

    private static final class Placement {
        final boolean[] occupied;
        final Map<Integer, Routine> startRoutineBySlotIndex;
        final Map<Integer, Integer> spanByStartSlotIndex;

        private Placement(int size) {
            this.occupied = new boolean[size];
            this.startRoutineBySlotIndex = new HashMap<>();
            this.spanByStartSlotIndex = new HashMap<>();
        }
    }

    private static Placement computePlacement(List<TimetableSlots.Slot> slots, List<Routine> routines) {
        int size = slots == null ? 0 : slots.size();
        Placement placement = new Placement(size);
        if (size == 0 || routines == null || routines.isEmpty()) return placement;

        List<Routine> sorted = new ArrayList<>(routines);
        sorted.removeIf(r -> r == null || r.getTimeStart() == null || r.getTimeEnd() == null);
        sorted.sort(Comparator.comparing(Routine::getTimeStart).thenComparing(Routine::getTimeEnd));

        for (Routine r : sorted) {
            int startIdx = findStartSlotIndex(slots, r.getTimeStart(), r.getTimeEnd());
            int endIdx = findEndSlotIndex(slots, r.getTimeStart(), r.getTimeEnd());
            if (startIdx < 0 || endIdx < 0 || endIdx < startIdx) continue;

            // If there is already a routine starting in the same slot, keep the longer one.
            Routine existing = placement.startRoutineBySlotIndex.get(startIdx);
            if (existing != null) {
                long existingMinutes = java.time.Duration.between(existing.getTimeStart(), existing.getTimeEnd()).toMinutes();
                long candidateMinutes = java.time.Duration.between(r.getTimeStart(), r.getTimeEnd()).toMinutes();
                if (candidateMinutes <= existingMinutes) {
                    continue;
                }
            }

            int span = (endIdx - startIdx) + 1;

            // Mark occupied and register the start routine.
            for (int i = startIdx; i <= endIdx && i < size; i++) {
                placement.occupied[i] = true;
            }
            placement.startRoutineBySlotIndex.put(startIdx, r);
            placement.spanByStartSlotIndex.put(startIdx, span);
        }

        return placement;
    }

    private static boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        // Overlap if A starts before B ends AND A ends after B starts.
        return startA.isBefore(endB) && endA.isAfter(startB);
    }

    private static int findStartSlotIndex(List<TimetableSlots.Slot> slots, LocalTime routineStart, LocalTime routineEnd) {
        for (int i = 0; i < slots.size(); i++) {
            TimetableSlots.Slot slot = slots.get(i);
            if (overlaps(routineStart, routineEnd, slot.start(), slot.end())) {
                return i;
            }
        }
        return -1;
    }

    private static int findEndSlotIndex(List<TimetableSlots.Slot> slots, LocalTime routineStart, LocalTime routineEnd) {
        int last = -1;
        for (int i = 0; i < slots.size(); i++) {
            TimetableSlots.Slot slot = slots.get(i);
            if (overlaps(routineStart, routineEnd, slot.start(), slot.end())) {
                last = i;
            }
        }
        return last;
    }

    private static void showOnlyOne(ContextMenu menu, VBox anchor, double screenX, double screenY) {
        if (activeMenu != null && activeMenu.isShowing()) {
            activeMenu.hide();
        }
        activeMenu = menu;
        menu.show(anchor, screenX, screenY);
    }

    private void openAddRoutine(String day, TimetableSlots.Slot slot) {
        if (classroom == null || grid == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-routine.fxml"));
            Parent root = loader.load();

            AddRoutineController ctrl = loader.getController();
            ctrl.setClassroom(classroom);
            ctrl.loadData();
            ctrl.setInitialDay(day);
            if (slot != null) {
                ctrl.setInitialTime(slot.start(), slot.end());
            }

            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) grid.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            com.classbuddy.util.ViewTransitions.fadeIn(root);
        } catch (IOException ignored) {
        }
    }

    private void openEditRoutine(Routine routine) {
        if (classroom == null || routine == null || grid == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-routine.fxml"));
            Parent root = loader.load();

            EditRoutineController ctrl = loader.getController();
            ctrl.setClassroom(classroom);
            ctrl.setRoutine(routine);
            ctrl.loadData();

            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) grid.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            com.classbuddy.util.ViewTransitions.fadeIn(root);
        } catch (IOException ignored) {
        }
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
