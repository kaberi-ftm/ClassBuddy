package com.classbuddy.controller;

import com.classbuddy.model.Classroom;
import com.classbuddy.model.User;
import com.classbuddy.service.AnalyticsService;
import com.classbuddy.service.ClassroomService;
import com.classbuddy.util.ViewTransitions;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ListView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AdminAnalyticsController {

    @FXML private ComboBox<Classroom> classroomCombo;
    @FXML private Label upcomingExamsLabel;
    @FXML private Label upcomingCTLabel;
    @FXML private Label upcomingLabsLabel;
    @FXML private Label avgExamLabel;
    @FXML private Label avgCTLabel;
    @FXML private Label avgLabLabel;
    @FXML private Label attendanceLabel;
    @FXML private ListView<String> courseAvgList;
    @FXML private PieChart gradePie;

    private User currentAdmin;
    private List<Classroom> adminClasses;

    @FXML
    public void initialize() {
        currentAdmin = LoginController.getCurrentUser();
        if (currentAdmin == null || currentAdmin.getRole() != com.classbuddy.model.Role.ADMIN) {
            return;
        }
        adminClasses = ClassroomService.getAdminClassrooms(currentAdmin.getId());
        classroomCombo.setItems(FXCollections.observableArrayList(adminClasses));
        classroomCombo.getSelectionModel().selectedItemProperty().addListener((obs, o, v) -> refreshMetrics());
        if (!adminClasses.isEmpty()) {
            classroomCombo.getSelectionModel().selectFirst();
            refreshMetrics();
        }
    }

    private void refreshMetrics() {
        Classroom c = classroomCombo.getValue();
        if (c == null) return;
        int upcomingDays = 14;
        upcomingExamsLabel.setText(String.valueOf(AnalyticsService.countUpcomingExams(c.getId(), upcomingDays)));
        upcomingCTLabel.setText(String.valueOf(AnalyticsService.countUpcomingCTs(c.getId(), upcomingDays)));
        upcomingLabsLabel.setText(String.valueOf(AnalyticsService.countUpcomingLabs(c.getId(), upcomingDays)));

        Double ex = AnalyticsService.averageExamPercent(c.getId());
        Double ct = AnalyticsService.averageCTPercent(c.getId());
        Double lab = AnalyticsService.averageLabPercent(c.getId());
        avgExamLabel.setText(ex == null ? "--" : String.format("%.1f%%", ex));
        avgCTLabel.setText(ct == null ? "--" : String.format("%.1f%%", ct));
        avgLabLabel.setText(lab == null ? "--" : String.format("%.1f%%", lab));

        Map<String,Integer> dist = AnalyticsService.examGradeDistribution(c.getId());
        gradePie.getData().clear();
        dist.forEach((g, cnt) -> gradePie.getData().add(new PieChart.Data(g, cnt)));

        Double attendancePct = AnalyticsService.overallAttendancePercent(c.getId());
        attendanceLabel.setText(attendancePct == null ? "--" : String.format("%.1f%%", attendancePct));

        courseAvgList.getItems().clear();
        AnalyticsService.courseExamAverages(c.getId()).forEach((course, pct) -> {
            courseAvgList.getItems().add(course + " — " + String.format("%.1f%%", pct));
        });
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
        }
    }
}
