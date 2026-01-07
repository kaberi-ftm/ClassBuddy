package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import com.classbuddy.util.ViewTransitions;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com.classbuddy.model.Routine;
import com.classbuddy.service.RoutineService;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.layout.VBox;

public class EditRoutineController {
    @FXML
    private Label classroomNameLabel;

    @FXML
    private ToggleGroup scheduleModeGroup;
    @FXML
    private RadioButton specificDayRadio;
    @FXML
    private RadioButton fullWeekRadio;
    @FXML
    private RadioButton customDaysRadio;

    @FXML
    private VBox specificDayBox;
    @FXML
    private VBox fullWeekBox;
    @FXML
    private VBox customDaysBox;

    @FXML
    private ComboBox<String> dayComboBox;

    @FXML
    private CheckBox mondayCheck;
    @FXML
    private CheckBox tuesdayCheck;
    @FXML
    private CheckBox wednesdayCheck;
    @FXML
    private CheckBox thursdayCheck;
    @FXML
    private CheckBox fridayCheck;
    @FXML
    private CheckBox saturdayCheck;
    @FXML
    private CheckBox sundayCheck;

    @FXML
    private TextField periodNumberField;
    @FXML
    private TextField courseNameField;
    @FXML
    private TextField teacherNameField;
    @FXML
    private TextField roomField;
    @FXML
    private TextField timeStartField;
    @FXML
    private TextField timeEndField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    private Classroom classroom;
    private Routine routine;

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public void setRoutine(Routine routine) {
        this.routine = routine;
    }

    @FXML
    public void initialize() {
        // Initialize day dropdown
        dayComboBox.getItems().addAll(
                "Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday", "Sunday"
        );
        dayComboBox.setValue("Monday");

        if (specificDayRadio != null) {
            specificDayRadio.setSelected(true);
        }

        if (scheduleModeGroup != null) {
            scheduleModeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> updateScheduleModeUI());
        }

        updateScheduleModeUI();
    }

    public void loadData() {
        if (classroom != null) {
            classroomNameLabel.setText(classroom.getName());
        }

        if (routine != null) {
            // Pre-fill form with routine data
            periodNumberField.setText(String.valueOf(routine.getPeriodNumber()));
            courseNameField.setText(routine.getCourseName());
            teacherNameField.setText(routine.getTeacherName());
            roomField.setText(routine.getRoom());
            timeStartField.setText(routine.getTimeStart().format(DateTimeFormatter.ofPattern("HH:mm")));
            timeEndField.setText(routine.getTimeEnd().format(DateTimeFormatter.ofPattern("HH:mm")));

            // Determine schedule mode based on applicable days
            List<String> applicableDays = routine.getApplicableDays();
            if (applicableDays != null && applicableDays.size() == 7) {
                // Full week
                fullWeekRadio.setSelected(true);
            } else if (applicableDays != null && applicableDays.size() > 1) {
                // Custom days
                customDaysRadio.setSelected(true);
                // Check appropriate checkboxes
                setCheckboxesFromApplicableDays(applicableDays);
            } else {
                // Specific day
                specificDayRadio.setSelected(true);
                if (applicableDays != null && !applicableDays.isEmpty()) {
                    dayComboBox.setValue(applicableDays.get(0));
                }
            }

            updateScheduleModeUI();
        }
    }

    private void setCheckboxesFromApplicableDays(List<String> days) {
        for (String day : days) {
            switch (day) {
                case "Monday" -> mondayCheck.setSelected(true);
                case "Tuesday" -> tuesdayCheck.setSelected(true);
                case "Wednesday" -> wednesdayCheck.setSelected(true);
                case "Thursday" -> thursdayCheck.setSelected(true);
                case "Friday" -> fridayCheck.setSelected(true);
                case "Saturday" -> saturdayCheck.setSelected(true);
                case "Sunday" -> sundayCheck.setSelected(true);
            }
        }
    }

    @FXML
    public void handleUpdateRoutine() {
        try {
            // For edit, we only update single day (simplified)
            String daySelected = dayComboBox.getValue();
            if (daySelected == null || daySelected.trim().isEmpty()) {
                showError("Please select a day");
                return;
            }

            String periodText = periodNumberField.getText().trim();
            String courseName = courseNameField.getText().trim();
            String teacherName = teacherNameField.getText().trim();
            String room = roomField.getText().trim();
            String timeStartText = timeStartField.getText().trim();
            String timeEndText = timeEndField.getText().trim();

            // Validation
            if (courseName.isEmpty()) {
                showError("Course name is required");
                return;
            }

            if (periodText.isEmpty()) {
                showError("Period number is required");
                return;
            }

            if (timeStartText.isEmpty()) {
                showError("Start time is required");
                return;
            }

            if (timeEndText.isEmpty()) {
                showError("End time is required");
                return;
            }

            // Validate time format before parsing
            if (!timeStartText.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                showError("Invalid start time format. Use HH:mm (e.g., 09:00)");
                return;
            }

            if (!timeEndText.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                showError("Invalid end time format. Use HH:mm (e.g., 10:00)");
                return;
            }

            // Parse values
            int periodNumber = Integer.parseInt(periodText);
            LocalTime timeStart = LocalTime.parse(timeStartText,
                    DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime timeEnd = LocalTime.parse(timeEndText,
                    DateTimeFormatter.ofPattern("HH:mm"));

            if (timeEnd.isBefore(timeStart) || timeEnd.equals(timeStart)) {
                showError("End time must be after start time");
                return;
            }

            // Update routine
            boolean updated = RoutineService.updateRoutine(
                    routine.getId(), daySelected, periodNumber, courseName,
                    teacherName, room, timeStart, timeEnd
            );

            if (updated) {
                showSuccess("Routine updated successfully");

                new Thread(() -> {
                    try {
                        Thread.sleep(900);
                    } catch (InterruptedException ignored) {
                    }

                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else {
                showError("Failed to update routine");
            }

        } catch (NumberFormatException e) {
            showError("Period number must be a valid number");
        } catch (Exception e) {
            showError("Invalid time format. Use HH:mm (e.g., 09:00)");
        }
    }

    @FXML
    public void handleDeleteRoutine() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Routine");
        alert.setHeaderText("Delete Routine?");
        alert.setContentText("Are you sure you want to delete this routine? This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean deleted = RoutineService.deleteRoutine(routine.getId());

            if (deleted) {
                showSuccess("Routine deleted successfully");

                new Thread(() -> {
                    try {
                        Thread.sleep(900);
                    } catch (InterruptedException ignored) {
                    }

                    javafx.application.Platform.runLater(this::goBack);
                }).start();
            } else {
                showError("Failed to delete routine");
            }
        }
    }

    @FXML
    public void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/classroom-detail.fxml")
            );
            Parent root = loader.load();

            ClassroomDetailController controller = loader.getController();
            controller.setClassroom(classroom);
            controller.setUser(LoginController.getCurrentUser());
            controller.loadData();

            Scene scene = new Scene(root, 1200, 800);
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goToAddExam() {
        navigateToAddScreen("/fxml/add-exam.fxml");
    }

    @FXML
    public void goToAddCTQuiz() {
        navigateToAddScreen("/fxml/add-ctquiz.fxml");
    }

    @FXML
    public void goToAddLabTest() {
        navigateToAddScreen("/fxml/add-labtest.fxml");
    }

    @FXML
    public void goToAddNotice() {
        navigateToAddScreen("/fxml/add-notice.fxml");
    }

    private void navigateToAddScreen(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AddExamController) {
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
            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            ViewTransitions.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateScheduleModeUI() {
        if (specificDayBox == null || fullWeekBox == null || customDaysBox == null) {
            return;
        }

        boolean isSpecificDay = specificDayRadio != null && specificDayRadio.isSelected();
        boolean isFullWeek = fullWeekRadio != null && fullWeekRadio.isSelected();
        boolean isCustomDays = customDaysRadio != null && customDaysRadio.isSelected();

        specificDayBox.setVisible(isSpecificDay);
        specificDayBox.setManaged(isSpecificDay);

        fullWeekBox.setVisible(isFullWeek);
        fullWeekBox.setManaged(isFullWeek);

        customDaysBox.setVisible(isCustomDays);
        customDaysBox.setManaged(isCustomDays);
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: -error;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: -success;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
