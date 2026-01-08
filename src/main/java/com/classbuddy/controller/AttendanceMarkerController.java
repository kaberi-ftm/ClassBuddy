package com.classbuddy.controller;

import com.classbuddy.model.Attendance;
import com.classbuddy.model.Classroom;
import com.classbuddy.model.User;
import com.classbuddy.service.AttendanceService;
import com.classbuddy.util.ViewTransitions;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for marking student attendance
 */
public class AttendanceMarkerController {

    @FXML private Label classroomNameLabel;
    @FXML private Label dateLabel;
    @FXML private DatePicker datePicker;
    @FXML private TableView<StudentAttendanceRow> attendanceTable;
    @FXML private TableColumn<StudentAttendanceRow, String> rollColumn;
    @FXML private TableColumn<StudentAttendanceRow, String> nameColumn;
    @FXML private TableColumn<StudentAttendanceRow, Attendance.Status> statusColumn;
    @FXML private Button markAllPresentButton;
    @FXML private Button saveButton;
    @FXML private Label statusLabel;

    private Classroom classroom;
    private LocalDate selectedDate;
    private ObservableList<StudentAttendanceRow> studentRows;

    /**
     * Inner class to represent a row in the attendance table
     */
    public static class StudentAttendanceRow {
        private final int studentId;
        private final SimpleStringProperty rollNumber;
        private final SimpleStringProperty studentName;
        private final SimpleObjectProperty<Attendance.Status> status;

        public StudentAttendanceRow(int studentId, String rollNumber, String studentName, Attendance.Status status) {
            this.studentId = studentId;
            this.rollNumber = new SimpleStringProperty(rollNumber);
            this.studentName = new SimpleStringProperty(studentName);
            this.status = new SimpleObjectProperty<>(status);
        }

        public int getStudentId() { return studentId; }
        public String getRollNumber() { return rollNumber.get(); }
        public String getStudentName() { return studentName.get(); }
        public Attendance.Status getStatus() { return status.get(); }
        public void setStatus(Attendance.Status status) { this.status.set(status); }
        public SimpleObjectProperty<Attendance.Status> statusProperty() { return status; }
    }

    @FXML
    public void initialize() {
        // Setup table columns
        rollColumn.setCellValueFactory(data -> data.getValue().rollNumber);
        nameColumn.setCellValueFactory(data -> data.getValue().studentName);
        
        // Status column with ComboBox
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        statusColumn.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<Attendance.Status> comboBox = new ComboBox<>();

            {
                comboBox.setItems(FXCollections.observableArrayList(Attendance.Status.values()));
                comboBox.setOnAction(event -> {
                    StudentAttendanceRow row = getTableRow().getItem();
                    if (row != null) {
                        row.setStatus(comboBox.getValue());
                    }
                });
            }

            @Override
            protected void updateItem(Attendance.Status item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    comboBox.setValue(item);
                    setGraphic(comboBox);
                }
            }
        });

        // Initialize date picker
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
            datePicker.setOnAction(e -> loadAttendanceForDate(datePicker.getValue()));
        }
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
        if (classroomNameLabel != null) {
            classroomNameLabel.setText(classroom.getName() + " - " + classroom.getSection());
        }
    }

    public void setDate(LocalDate date) {
        this.selectedDate = date;
        if (dateLabel != null) {
            dateLabel.setText(date.toString());
        }
        if (datePicker != null) {
            datePicker.setValue(date);
        }
        loadAttendanceForDate(date);
    }

    public void loadData() {
        if (selectedDate == null) {
            selectedDate = LocalDate.now();
        }
        loadAttendanceForDate(selectedDate);
    }

    private void loadAttendanceForDate(LocalDate date) {
        if (classroom == null) return;
        
        this.selectedDate = date;
        studentRows = FXCollections.observableArrayList();

        // Get all active students in classroom
        List<StudentInfo> students = getActiveStudents(classroom.getId());
        
        // Get existing attendance for this date
        List<Attendance> existingAttendance = AttendanceService.getAttendanceForDate(classroom.getId(), date);
        
        // Create rows
        for (StudentInfo student : students) {
            Attendance.Status status = Attendance.Status.ABSENT; // Default
            
            // Check if attendance already marked
            for (Attendance att : existingAttendance) {
                if (att.getStudentId() == student.studentId) {
                    status = att.getStatus();
                    break;
                }
            }
            
            studentRows.add(new StudentAttendanceRow(
                student.studentId,
                student.rollNumber,
                student.studentName,
                status
            ));
        }

        attendanceTable.setItems(studentRows);
        
        // Update status
        boolean alreadyMarked = !existingAttendance.isEmpty();
        statusLabel.setText(alreadyMarked ? 
            "Attendance already marked for this date. You can update it." : 
            "No attendance marked yet for this date.");
        statusLabel.setStyle(alreadyMarked ? "-fx-text-fill: #3498db;" : "-fx-text-fill: #7f8c8d;");
    }

    @FXML
    private void handleMarkAllPresent() {
        for (StudentAttendanceRow row : studentRows) {
            row.setStatus(Attendance.Status.PRESENT);
        }
        attendanceTable.refresh();
    }

    @FXML
    private void handleSave() {
        if (classroom == null || selectedDate == null) {
            showError("Missing classroom or date information");
            return;
        }

        User currentUser = LoginController.getCurrentUser();
        if (currentUser == null) {
            showError("No user logged in");
            return;
        }

        // Create attendance list
        List<Attendance> attendanceList = new ArrayList<>();
        for (StudentAttendanceRow row : studentRows) {
            attendanceList.add(new Attendance(
                classroom.getId(),
                row.getStudentId(),
                row.getRollNumber(),
                selectedDate,
                row.getStatus(),
                currentUser.getId()
            ));
        }

        // Save to database
        int savedCount = AttendanceService.markBulkAttendance(
            classroom.getId(),
            selectedDate,
            attendanceList,
            currentUser.getId()
        );

        if (savedCount > 0) {
            statusLabel.setText("✓ Attendance saved successfully! " + savedCount + " records updated.");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            
            // Refresh after short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::goBack);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("Failed to save attendance. Please try again.");
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/classroom-detail.fxml"));
            Parent root = loader.load();

            ClassroomDetailController controller = loader.getController();
            controller.setClassroom(classroom);
            controller.loadData();

            Scene scene = new Scene(root, 1600, 900);
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to return to classroom: " + e.getMessage());
        }
    }

    /**
     * Helper class for student info
     */
    private static class StudentInfo {
        int studentId;
        String rollNumber;
        String studentName;

        StudentInfo(int studentId, String rollNumber, String studentName) {
            this.studentId = studentId;
            this.rollNumber = rollNumber;
            this.studentName = studentName;
        }
    }

    /**
     * Get active students in classroom
     */
    private List<StudentInfo> getActiveStudents(int classroomId) {
        List<StudentInfo> students = new ArrayList<>();
        String sql = "SELECT cs.student_id, cs.roll_number, u.username " +
                    "FROM classroom_students cs " +
                    "JOIN users u ON cs.student_id = u.id " +
                    "WHERE cs.classroom_id = ? AND cs.enrollment_status = 'ACTIVE' " +
                    "ORDER BY cs.roll_number";

        try (Connection conn = com.classbuddy.util.DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, classroomId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                students.add(new StudentInfo(
                    rs.getInt("student_id"),
                    rs.getString("roll_number"),
                    rs.getString("username")
                ));
            }

        } catch (Exception e) {
            System.err.println("Error fetching students: " + e.getMessage());
            e.printStackTrace();
        }

        return students;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
