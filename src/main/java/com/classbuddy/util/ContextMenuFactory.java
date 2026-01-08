package com.classbuddy.util;

import com.classbuddy.controller.AddCTQuizController;
import com.classbuddy.controller.AddExamController;
import com.classbuddy.controller.AddLabTestController;
import com.classbuddy.controller.AddNoticeController;
import com.classbuddy.controller.AddRoutineController;
import com.classbuddy.controller.EditExamController;
import com.classbuddy.controller.EditNoticeController;
import com.classbuddy.model.Classroom;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ContextMenuFactory {

    private static ContextMenu activeMenu;

    public static void attachAdminMenu(VBox dayCell, LocalDate date, List<Classroom> classrooms) {
        dayCell.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            ContextMenu menu = buildMenuForDate(date, classrooms, dayCell, true);
            showOnlyOne(menu, dayCell, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    public static void attachStudentMenu(VBox dayCell, LocalDate date, List<Classroom> classrooms) {
        dayCell.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> {
            ContextMenu menu = buildMenuForDate(date, classrooms, dayCell, false);
            showOnlyOne(menu, dayCell, e.getScreenX(), e.getScreenY());
            e.consume();
        });
    }

    private static void showOnlyOne(ContextMenu menu, VBox dayCell, double screenX, double screenY) {
        if (activeMenu != null && activeMenu.isShowing()) {
            activeMenu.hide();
        }
        activeMenu = menu;
        menu.show(dayCell, screenX, screenY);
    }

    private static ContextMenu buildMenuForDate(LocalDate date, List<Classroom> classrooms, VBox dayCell, boolean isAdmin) {
        ContextMenu menu = new ContextMenu();

        if (classrooms == null || classrooms.isEmpty()) {
            MenuItem disabled = new MenuItem("No classroom context");
            disabled.setDisable(true);
            menu.getItems().addAll(disabled);
            return menu;
        }

        // Add quick add actions
        if (classrooms.size() == 1) {
            // Integrated (single-classroom) calendar: no need to choose a classroom
            Classroom classroom = classrooms.get(0);
            menu.getItems().addAll(
                    createNavigateItemForDate("Add Routine", "/fxml/add-routine.fxml", dayCell, classroom, date),
                    createNavigateItemForDate("Add Exam", "/fxml/add-exam.fxml", dayCell, classroom, date),
                    createNavigateItemForDate("Add Notice", "/fxml/add-notice.fxml", dayCell, classroom, date)
            );
        } else {
            Menu addRoutine = new Menu("Add Routine");
            Menu addExam = new Menu("Add Exam");
            Menu addNotice = new Menu("Add Notice");

            for (Classroom c : classrooms) {
                addRoutine.getItems().add(createNavigateItemForDate("Add to " + c.getName(), "/fxml/add-routine.fxml", dayCell, c, date));
                addExam.getItems().add(createNavigateItemForDate("Add to " + c.getName(), "/fxml/add-exam.fxml", dayCell, c, date));
                addNotice.getItems().add(createNavigateItemForDate("Add to " + c.getName(), "/fxml/add-notice.fxml", dayCell, c, date));
            }

            menu.getItems().addAll(addRoutine, addExam, addNotice);
        }

        // If admin: check for existing events and add Edit/Delete options
        if (isAdmin && classrooms.size() == 1) {
            // Only show edit/delete for single-classroom context (for now)
            Classroom classroom = classrooms.get(0);
            
            // Check for existing events/routines on this date
            boolean hasEvents = false;

            // Check for exams
            List<com.classbuddy.model.Exam> dayExams = getDayExams(classroom.getId(), date);
            if (!dayExams.isEmpty()) {
                menu.getItems().add(new SeparatorMenuItem());
                for (com.classbuddy.model.Exam exam : dayExams) {
                    MenuItem editExam = new MenuItem("Edit: " + exam.getCourseName());
                    editExam.setOnAction(ev -> openEditExamScreen(dayCell, classroom, exam));
                    menu.getItems().add(editExam);
                }
                hasEvents = true;
            }

            // Check for notices
            List<com.classbuddy.model.Notice> dayNotices = getDayNotices(classroom.getId(), date);
            if (!dayNotices.isEmpty()) {
                if (!hasEvents) menu.getItems().add(new SeparatorMenuItem());
                for (com.classbuddy.model.Notice notice : dayNotices) {
                    MenuItem editNotice = new MenuItem("Edit: " + notice.getTitle());
                    editNotice.setOnAction(ev -> openEditNoticeScreen(dayCell, classroom, notice));
                    menu.getItems().add(editNotice);
                }
            }
        }

        return menu;
    }

    private static List<com.classbuddy.model.Exam> getDayExams(int classroomId, LocalDate date) {
        // Query exams for this classroom on the given date
        try {
            java.sql.Connection conn = com.classbuddy.util.DatabaseUtil.getConnection();
            String sql = "SELECT id, classroom_id, course_name, exam_type, exam_date, exam_time, room, created_at FROM exam WHERE classroom_id = ? AND exam_date = ?";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, classroomId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            List<com.classbuddy.model.Exam> exams = new java.util.ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String courseName = rs.getString("course_name");
                String examType = rs.getString("exam_type");
                java.time.LocalDate examDate = rs.getDate("exam_date").toLocalDate();
                java.time.LocalTime examTime = rs.getTime("exam_time").toLocalTime();
                String room = rs.getString("room");
                java.time.LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                
                exams.add(new com.classbuddy.model.Exam(id, classroomId, courseName, examType, examDate, examTime, room, createdAt));
            }
            rs.close();
            pstmt.close();
            conn.close();
            return exams;
        } catch (java.sql.SQLException e) {
            return List.of();
        }
    }

    private static List<com.classbuddy.model.Notice> getDayNotices(int classroomId, LocalDate date) {
        // Query notices for this classroom (recent ones)
        try {
            java.sql.Connection conn = com.classbuddy.util.DatabaseUtil.getConnection();
            String sql = "SELECT id, classroom_id, title, content, category, is_pinned, created_by, created_at FROM notice WHERE classroom_id = ? ORDER BY created_at DESC LIMIT 10";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, classroomId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            List<com.classbuddy.model.Notice> notices = new java.util.ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String category = rs.getString("category");
                boolean isPinned = rs.getBoolean("is_pinned");
                int createdBy = rs.getInt("created_by");
                java.time.LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                
                notices.add(new com.classbuddy.model.Notice(id, classroomId, title, content, category, isPinned, createdBy, createdAt));
            }
            rs.close();
            pstmt.close();
            conn.close();
            return notices;
        } catch (java.sql.SQLException e) {
            return List.of();
        }
    }

    private static void openEditExamScreen(VBox dayCell, Classroom classroom, com.classbuddy.model.Exam exam) {
        try {
            FXMLLoader loader = new FXMLLoader(ContextMenuFactory.class.getResource("/fxml/edit-exam.fxml"));
            Parent root = loader.load();

            EditExamController ctrl = loader.getController();
            ctrl.setClassroom(classroom);
            ctrl.setExam(exam);
            ctrl.loadData();

            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) dayCell.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void openEditNoticeScreen(VBox dayCell, Classroom classroom, com.classbuddy.model.Notice notice) {
        try {
            FXMLLoader loader = new FXMLLoader(ContextMenuFactory.class.getResource("/fxml/edit-notice.fxml"));
            Parent root = loader.load();

            EditNoticeController ctrl = loader.getController();
            ctrl.setClassroom(classroom);
            ctrl.setNotice(notice);
            ctrl.loadData();

            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) dayCell.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MenuItem createNavigateItemForDate(String text, String fxmlPath, VBox dayCell, Classroom classroom, LocalDate date) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(ev -> navigateToAddScreen(fxmlPath, dayCell, classroom, date));
        return item;
    }

    private static void navigateToAddScreen(String fxmlPath, VBox dayCell, Classroom classroom, LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(ContextMenuFactory.class.getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AddRoutineController) {
                AddRoutineController ctrl = (AddRoutineController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            } else if (controller instanceof AddExamController) {
                AddExamController ctrl = (AddExamController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
                if (date != null) {
                    ctrl.setInitialDate(date);
                }
            } else if (controller instanceof AddCTQuizController) {
                AddCTQuizController ctrl = (AddCTQuizController) controller;
                ctrl.setClassroom(classroom);
                if (date != null) {
                    ctrl.setInitialDeadline(date);
                }
            } else if (controller instanceof AddLabTestController) {
                AddLabTestController ctrl = (AddLabTestController) controller;
                ctrl.setClassroom(classroom);
                if (date != null) {
                    ctrl.setInitialTestDate(date);
                }
            } else if (controller instanceof AddNoticeController) {
                AddNoticeController ctrl = (AddNoticeController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            }

            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) dayCell.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
