package com.classbuddy.util;

import com.classbuddy.controller.*;
import com.classbuddy.model.Classroom;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ContextMenuFactory {

    public static void attachAdminMenu(VBox dayCell, LocalDate date, List<Classroom> classrooms) {
        ContextMenu menu = buildAddMenu(classrooms, dayCell);
        dayCell.setOnContextMenuRequested(e -> {
            menu.show(dayCell, e.getScreenX(), e.getScreenY());
        });
    }

    public static void attachStudentMenu(VBox dayCell, LocalDate date, List<Classroom> classrooms) {
        ContextMenu menu = buildAddMenu(classrooms, dayCell);
        dayCell.setOnContextMenuRequested(e -> {
            menu.show(dayCell, e.getScreenX(), e.getScreenY());
        });
    }

    private static ContextMenu buildAddMenu(List<Classroom> classrooms, VBox dayCell) {
        ContextMenu menu = new ContextMenu();

        Menu addRoutine = new Menu("Add Routine");
        Menu addExam = new Menu("Add Exam");
        Menu addCTQuiz = new Menu("Add Test (CT/Quiz)");
        Menu addLabTest = new Menu("Add Lab Test");
        Menu addNotice = new Menu("Add Notice");

        if (classrooms == null || classrooms.isEmpty()) {
            MenuItem disabled = new MenuItem("No classroom context");
            disabled.setDisable(true);
            menu.getItems().addAll(disabled);
            return menu;
        }

        for (Classroom c : classrooms) {
            addRoutine.getItems().add(createNavigateItem("Add to " + c.getName(), "/fxml/add-routine.fxml", dayCell, c));
            addExam.getItems().add(createNavigateItem("Add to " + c.getName(), "/fxml/add-exam.fxml", dayCell, c));
            addCTQuiz.getItems().add(createNavigateItem("Add to " + c.getName(), "/fxml/add-ctquiz.fxml", dayCell, c));
            addLabTest.getItems().add(createNavigateItem("Add to " + c.getName(), "/fxml/add-labtest.fxml", dayCell, c));
            addNotice.getItems().add(createNavigateItem("Add to " + c.getName(), "/fxml/add-notice.fxml", dayCell, c));
        }

        menu.getItems().addAll(addRoutine, addExam, addCTQuiz, addLabTest, addNotice);
        return menu;
    }

    private static MenuItem createNavigateItem(String text, String fxmlPath, VBox dayCell, Classroom classroom) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(ev -> navigateToAddScreen(fxmlPath, dayCell, classroom));
        return item;
    }

    private static void navigateToAddScreen(String fxmlPath, VBox dayCell, Classroom classroom) {
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
            } else if (controller instanceof AddCTQuizController) {
                AddCTQuizController ctrl = (AddCTQuizController) controller;
                ctrl.setClassroom(classroom);
            } else if (controller instanceof AddLabTestController) {
                AddLabTestController ctrl = (AddLabTestController) controller;
                ctrl.setClassroom(classroom);
            } else if (controller instanceof AddNoticeController) {
                AddNoticeController ctrl = (AddNoticeController) controller;
                ctrl.setClassroom(classroom);
                ctrl.loadData();
            }

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) dayCell.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
