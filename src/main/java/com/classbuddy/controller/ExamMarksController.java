package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.model.CTQuiz;
import com.classbuddy.model.Exam;
import com.classbuddy.model.LabTest;
import com.classbuddy.model.User;
import com.classbuddy.service.ClassroomService;
import com.classbuddy.service.CTQuizService;
import com.classbuddy.service.ExamService;
import com.classbuddy.service.GradesService;
import com.classbuddy.service.LabTestService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import com.classbuddy.util.ViewTransitions;
import java.util.List;

public class ExamMarksController {

    @FXML private ComboBox<Classroom> classroomCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<Exam> examCombo;
    @FXML private ComboBox<CTQuiz> ctCombo;
    @FXML private ComboBox<LabTest> labCombo;
    @FXML private TextField rollField;
    @FXML private TextField scoreField;
    @FXML private TextField totalField;
    @FXML private TextField gradeField;
    @FXML private TextArea remarksArea;
    @FXML private TextField filterRollField;
    @FXML private Label statusLabel;
    @FXML private TableView<RecentEntry> recentTable;
    @FXML private TableColumn<RecentEntry, String> typeCol;
    @FXML private TableColumn<RecentEntry, String> itemCol;
    @FXML private TableColumn<RecentEntry, String> rollCol;
    @FXML private TableColumn<RecentEntry, String> scoreCol;
    @FXML private TableColumn<RecentEntry, String> gradeCol;
    @FXML private TableColumn<RecentEntry, String> remarksCol;

    private List<Classroom> adminClasses;
    private User currentUser;
    private FilteredList<RecentEntry> filteredEntries;

    @FXML
    public void initialize() {
        currentUser = LoginController.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != com.classbuddy.model.Role.ADMIN) {
            statusLabel.setText("Admin only");
            return;
        }

        adminClasses = ClassroomService.getAdminClassrooms(currentUser.getId());
        classroomCombo.setItems(FXCollections.observableArrayList(adminClasses));
        typeCombo.setItems(FXCollections.observableArrayList("Exam", "CT/Quiz", "Lab Test"));
        typeCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> refreshTypeSelectors());
        classroomCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> refreshLists());

        if (recentTable != null) {
            typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().type()));
            itemCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().item()));
            rollCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().roll()));
            scoreCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().score()));
            gradeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().grade()));
            remarksCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().remarks()));

            recentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) loadEntryIntoForm(newSel);
            });
        }

        if (filterRollField != null) {
            filterRollField.textProperty().addListener((obs, old, val) -> applyFilter());
        }

        // Cell formatting
        classroomCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Classroom item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getSection() + ")");
            }
        });
        classroomCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Classroom item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getSection() + ")");
            }
        });

        examCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Exam item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCourseName() + " (" + item.getExamType() + ")");
            }
        });
        examCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Exam item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCourseName() + " (" + item.getExamType() + ")");
            }
        });

        ctCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(CTQuiz item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        ctCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(CTQuiz item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        labCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(LabTest item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getExperimentNumber());
            }
        });
        labCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LabTest item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getExperimentNumber());
            }
        });

        if (!adminClasses.isEmpty()) {
            classroomCombo.getSelectionModel().selectFirst();
            typeCombo.getSelectionModel().selectFirst();
            refreshLists();
            refreshTypeSelectors();
        }
    }

    private void refreshLists() {
        Classroom c = classroomCombo.getValue();
        if (c == null) return;
        ObservableList<Exam> exams = FXCollections.observableArrayList(ExamService.getClassroomExams(c.getId()));
        ObservableList<CTQuiz> cts = FXCollections.observableArrayList(CTQuizService.getClassroomCTQuizzes(c.getId()));
        ObservableList<LabTest> labs = FXCollections.observableArrayList(LabTestService.getClassroomLabTests(c.getId()));
        examCombo.setItems(exams);
        ctCombo.setItems(cts);
        labCombo.setItems(labs);
        if (!exams.isEmpty()) examCombo.getSelectionModel().selectFirst();
        if (!cts.isEmpty()) ctCombo.getSelectionModel().selectFirst();
        if (!labs.isEmpty()) labCombo.getSelectionModel().selectFirst();
        refreshTable();
    }

    private void refreshTypeSelectors() {
        String type = typeCombo.getValue();
        examCombo.setVisible("Exam".equals(type));
        ctCombo.setVisible("CT/Quiz".equals(type));
        labCombo.setVisible("Lab Test".equals(type));
    }

    @FXML
    private void handleSave() {
        Classroom c = classroomCombo.getValue();
        if (c == null) {
            statusLabel.setText("Select a classroom");
            return;
        }
        String roll = rollField.getText();
        if (roll == null || roll.isBlank()) {
            statusLabel.setText("Enter roll number");
            return;
        }

        Double score = parseDouble(scoreField.getText());
        Double total = parseDouble(totalField.getText());
        String grade = gradeField.getText();
        String remarks = remarksArea.getText();

        // Basic validation
        if ((score == null) ^ (total == null)) {
            statusLabel.setText("Provide both score and total, or leave both blank");
            return;
        }
        if (score != null && (score < 0)) {
            statusLabel.setText("Score cannot be negative");
            return;
        }
        if (total != null && (total <= 0)) {
            statusLabel.setText("Total must be greater than 0");
            return;
        }
        if (score != null && total != null && score > total) {
            statusLabel.setText("Score cannot exceed total");
            return;
        }

        // Auto-suggest grade if missing
        if ((grade == null || grade.isBlank()) && score != null && total != null && total > 0) {
            double pct = (score * 100.0) / total;
            String suggested;
            if (pct >= 80) suggested = "A+";
            else if (pct >= 70) suggested = "A";
            else if (pct >= 60) suggested = "B";
            else if (pct >= 50) suggested = "C";
            else if (pct >= 40) suggested = "D";
            else suggested = "F";
            grade = suggested;
            gradeField.setText(suggested);
        }

        String type = typeCombo.getValue();
        boolean ok = false;
        if ("Exam".equals(type) && examCombo.getValue() != null) {
            ok = GradesService.addExamResult(c.getId(), examCombo.getValue().getId(), roll.trim(), score, total, grade, remarks);
        } else if ("CT/Quiz".equals(type) && ctCombo.getValue() != null) {
            ok = GradesService.addCTQuizResult(c.getId(), ctCombo.getValue().getId(), roll.trim(), score, total, grade, remarks);
        } else if ("Lab Test".equals(type) && labCombo.getValue() != null) {
            ok = GradesService.addLabEvaluation(c.getId(), labCombo.getValue().getId(), roll.trim(), score, total, grade, remarks);
        }

        if (ok) {
            statusLabel.setText("Saved");
            refreshTable();
        } else {
            statusLabel.setText("Failed to save");
        }
    }

    private void refreshTable() {
        Classroom c = classroomCombo.getValue();
        if (c == null) return;
        String type = typeCombo.getValue();
        ObservableList<RecentEntry> rows = FXCollections.observableArrayList();
        if ("Exam".equals(type)) {
            GradesService.getExamResults(c.getId()).forEach(r -> rows.add(new RecentEntry(
                    "Exam",
                    "#" + r.getExamId(),
                    r.getRollNumber(),
                    formatScore(r.getScore(), r.getTotal()),
                    safe(r.getGrade()),
                    safe(r.getRemarks()),
                    r.getExamId(),
                    null,
                    null,
                    r.getScore(),
                    r.getTotal()
            )));
        } else if ("CT/Quiz".equals(type)) {
            GradesService.getCTResults(c.getId()).forEach(r -> rows.add(new RecentEntry(
                    "CT/Quiz",
                    "#" + r.getCtQuizId(),
                    r.getRollNumber(),
                    formatScore(r.getScore(), r.getTotal()),
                    safe(r.getGrade()),
                    safe(r.getRemarks()),
                    null,
                    r.getCtQuizId(),
                    null,
                    r.getScore(),
                    r.getTotal()
            )));
        } else if ("Lab Test".equals(type)) {
            GradesService.getLabEvaluations(c.getId()).forEach(r -> rows.add(new RecentEntry(
                    "Lab",
                    "#" + r.getLabTestId(),
                    r.getRollNumber(),
                    formatScore(r.getScore(), r.getTotal()),
                    safe(r.getGrade()),
                    safe(r.getRemarks()),
                    null,
                    null,
                    r.getLabTestId(),
                    r.getScore(),
                    r.getTotal()
            )));
        }
        filteredEntries = new FilteredList<>(rows, this::filterPredicate);
        recentTable.setItems(filteredEntries);
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private String formatScore(Double score, Double total) {
        if (score == null || total == null) return "";
        return score + "/" + total;
    }

    private boolean filterPredicate(RecentEntry e) {
        if (e == null) return false;
        String rollFilter = filterRollField == null ? "" : filterRollField.getText();
        if (rollFilter == null || rollFilter.isBlank()) return true;
        return e.roll().toLowerCase().contains(rollFilter.toLowerCase());
    }

    private void applyFilter() {
        if (filteredEntries != null) {
            filteredEntries.setPredicate(this::filterPredicate);
        }
    }

    private void loadEntryIntoForm(RecentEntry entry) {
        if (entry == null) return;
        rollField.setText(entry.roll());
        scoreField.setText(entry.scoreValue() == null ? "" : String.valueOf(entry.scoreValue()));
        totalField.setText(entry.totalValue() == null ? "" : String.valueOf(entry.totalValue()));
        gradeField.setText(entry.grade());
        remarksArea.setText(entry.remarks());

        if ("Exam".equals(entry.type()) && entry.examId() != null) {
            typeCombo.getSelectionModel().select("Exam");
            refreshTypeSelectors();
            examCombo.getItems().stream().filter(e -> e.getId() == entry.examId()).findFirst().ifPresent(examCombo::setValue);
        } else if ("CT/Quiz".equals(entry.type()) && entry.ctId() != null) {
            typeCombo.getSelectionModel().select("CT/Quiz");
            refreshTypeSelectors();
            ctCombo.getItems().stream().filter(e -> e.getId() == entry.ctId()).findFirst().ifPresent(ctCombo::setValue);
        } else if ("Lab".equals(entry.type()) && entry.labId() != null) {
            typeCombo.getSelectionModel().select("Lab Test");
            refreshTypeSelectors();
            labCombo.getItems().stream().filter(e -> e.getId() == entry.labId()).findFirst().ifPresent(labCombo::setValue);
        }
    }

    private Double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) classroomCombo.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to go back");
        }
    }

    public record RecentEntry(String type, String item, String roll, String score, String grade, String remarks,
                             Integer examId, Integer ctId, Integer labId, Double scoreValue, Double totalValue) {}
}
