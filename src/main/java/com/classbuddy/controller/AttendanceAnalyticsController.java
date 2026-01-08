package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.service.AttendanceService;
import com.classbuddy.service.AttendanceExportService;
import com.classbuddy.util.ViewTransitions;
import com.classbuddy.util.NavigationUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.classbuddy.util.DatabaseUtil;

/**
 * Controller for attendance analytics/reporting per student.
 */
public class AttendanceAnalyticsController {

    @FXML private Label classroomNameLabel;
    @FXML private Label exportStatusLabel;
    @FXML private TableView<StudentStatsRow> statsTable;
    @FXML private TableColumn<StudentStatsRow, String> rollColumn;
    @FXML private TableColumn<StudentStatsRow, String> nameColumn;
    @FXML private TableColumn<StudentStatsRow, Number> presentColumn;
    @FXML private TableColumn<StudentStatsRow, Number> absentColumn;
    @FXML private TableColumn<StudentStatsRow, Number> lateColumn;
    @FXML private TableColumn<StudentStatsRow, Number> percentageColumn;

    private Classroom classroom;
    private ObservableList<StudentStatsRow> rows;

    public static class StudentStatsRow {
        private final SimpleStringProperty rollNumber;
        private final SimpleStringProperty studentName;
        private final SimpleIntegerProperty present;
        private final SimpleIntegerProperty absent;
        private final SimpleIntegerProperty late;
        private final SimpleDoubleProperty percentage;

        public StudentStatsRow(String rollNumber, String studentName,
                               int present, int absent, int late, double percentage) {
            this.rollNumber = new SimpleStringProperty(rollNumber);
            this.studentName = new SimpleStringProperty(studentName);
            this.present = new SimpleIntegerProperty(present);
            this.absent = new SimpleIntegerProperty(absent);
            this.late = new SimpleIntegerProperty(late);
            this.percentage = new SimpleDoubleProperty(percentage);
        }

        public String getRollNumber() { return rollNumber.get(); }
        public String getStudentName() { return studentName.get(); }
        public int getPresent() { return present.get(); }
        public int getAbsent() { return absent.get(); }
        public int getLate() { return late.get(); }
        public double getPercentage() { return percentage.get(); }
    }

    @FXML
    public void initialize() {
        rollColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRollNumber()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        presentColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPresent()));
        absentColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAbsent()));
        lateColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getLate()));
        percentageColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPercentage()));

        rows = FXCollections.observableArrayList();
        statsTable.setItems(rows);
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroomNameLabel != null && classroom != null) {
            classroomNameLabel.setText(classroom.getName() + " (" + classroom.getSection() + ")");
        }
    }

    public void loadData() {
        if (classroom == null) return;
        rows.clear();
        List<StudentInfo> students = getActiveStudents(classroom.getId());
        for (StudentInfo s : students) {
            Map<String, Object> stats = AttendanceService.getAttendanceStats(s.studentId, classroom.getId());
            int present = (int) stats.getOrDefault("present", 0);
            int absent = (int) stats.getOrDefault("absent", 0);
            int late = (int) stats.getOrDefault("late", 0);
            double percentage = (double) stats.getOrDefault("percentage", 0.0);
            rows.add(new StudentStatsRow(s.rollNumber, s.name, present, absent, late, percentage));
        }
        statsTable.refresh();
    }

    private static class StudentInfo {
        final int studentId; String rollNumber; String name;
        StudentInfo(int studentId, String rollNumber, String name) {
            this.studentId = studentId; this.rollNumber = rollNumber; this.name = name;
        }
    }

    private List<StudentInfo> getActiveStudents(int classroomId) {
        String sql = "SELECT u.id as student_id, cs.roll_number, u.username " +
                "FROM classroom_students cs " +
                "JOIN users u ON cs.student_id = u.id " +
                "WHERE cs.classroom_id = ? AND cs.enrollment_status = 'ACTIVE' " +
                "ORDER BY cs.roll_number ASC";

        List<StudentInfo> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, classroomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentInfo(
                            rs.getInt("student_id"),
                            rs.getString("roll_number"),
                            rs.getString("username")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @FXML
    public void goBack() {
        try {
            Stage stage = (Stage) statsTable.getScene().getWindow();
            ClassroomDetailController controller = NavigationUtil.navigateWithController(
                    "/fxml/classroom-detail.fxml",
                    stage,
                    1366,
                    800
            );
            controller.setClassroom(classroom);
            controller.loadData();
            ViewTransitions.fadeIn(stage.getScene().getRoot());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportCsv() {
        if (classroom == null) return;
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Save Attendance Export");
        chooser.setInitialFileName("attendance_export.csv");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        Stage stage = (Stage) statsTable.getScene().getWindow();
        java.io.File file = chooser.showSaveDialog(stage);
        if (file == null) return;
        try {
            AttendanceExportService.ExportResult result = AttendanceExportService.exportClassroomAttendanceCSV(classroom.getId(), file.toPath());
            if (exportStatusLabel != null) {
                exportStatusLabel.setText("Exported " + result.getTotalRows() + " rows to " + result.getFilePath());
                exportStatusLabel.getStyleClass().removeAll("status-info", "status-muted", "status-success", "status-error");
                exportStatusLabel.getStyleClass().add("status-success");
            }
        } catch (Exception ex) {
            if (exportStatusLabel != null) {
                exportStatusLabel.setText("Export failed: " + ex.getMessage());
                exportStatusLabel.getStyleClass().removeAll("status-info", "status-muted", "status-success", "status-error");
                exportStatusLabel.getStyleClass().add("status-error");
            }
            ex.printStackTrace();
        }
    }
}
