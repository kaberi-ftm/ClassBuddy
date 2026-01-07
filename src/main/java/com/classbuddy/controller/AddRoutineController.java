package com.classbuddy.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import com.classbuddy.util.ViewTransitions;
import javafx.stage.Stage;
import com.classbuddy.model.Classroom;
import com.classbuddy.service.RoutineService;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.VBox;

public class AddRoutineController {
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

    private Classroom classroom;
    private Runnable onSuccessCallback;

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }



    public void setOnSuccess(Runnable callback) {
        this.onSuccessCallback = callback;
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
    }

    @FXML
    public void handleAddRoutine() {
        try {
            List<String> applicableDays = getApplicableDaysFromUI();
            if (applicableDays.isEmpty()) {
                showError("At least one day must be selected");
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

            // Add routine
            boolean added = RoutineService.addRoutine(
                    classroom.getId(), applicableDays, periodNumber, courseName,
                    teacherName, room, timeStart, timeEnd
            );

            if (added) {
                showSuccess("Routine added successfully");
                clearFields();

                if (specificDayRadio != null) specificDayRadio.setDisable(true);
                if (fullWeekRadio != null) fullWeekRadio.setDisable(true);
                if (customDaysRadio != null) customDaysRadio.setDisable(true);

                new Thread(() -> {
                    try {
                        Thread.sleep(900);
                    } catch (InterruptedException ignored) {
                    }

                    javafx.application.Platform.runLater(() -> {
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                            Stage stage = (Stage) classroomNameLabel.getScene().getWindow();
                            stage.close();
                        } else {
                            goBack();
                        }
                    });
                }).start();
            } else {
                showError("Failed to add routine");
            }

        } catch (NumberFormatException e) {
            showError("Period number must be a valid number");
        } catch (Exception e) {
            showError("Invalid time format. Use HH:mm (e.g., 09:00)");
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

    private void clearFields() {
        periodNumberField.clear();
        courseNameField.clear();
        teacherNameField.clear();
        roomField.clear();
        timeStartField.clear();
        timeEndField.clear();
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

    private List<String> getApplicableDaysFromUI() {
        if (fullWeekRadio != null && fullWeekRadio.isSelected()) {
            return List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        }

        if (customDaysRadio != null && customDaysRadio.isSelected()) {
            List<String> selected = new ArrayList<>();
            if (mondayCheck != null && mondayCheck.isSelected()) selected.add("Monday");
            if (tuesdayCheck != null && tuesdayCheck.isSelected()) selected.add("Tuesday");
            if (wednesdayCheck != null && wednesdayCheck.isSelected()) selected.add("Wednesday");
            if (thursdayCheck != null && thursdayCheck.isSelected()) selected.add("Thursday");
            if (fridayCheck != null && fridayCheck.isSelected()) selected.add("Friday");
            if (saturdayCheck != null && saturdayCheck.isSelected()) selected.add("Saturday");
            if (sundayCheck != null && sundayCheck.isSelected()) selected.add("Sunday");
            return selected;
        }

        String selectedDay = dayComboBox == null ? null : dayComboBox.getValue();
        if (selectedDay == null || selectedDay.trim().isEmpty()) {
            return List.of();
        }
        return List.of(selectedDay);
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