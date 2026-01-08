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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.scene.control.ListView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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
    @FXML private LineChart<String, Number> attendanceTrend;
    @FXML private LineChart<String, Number> examTrend;

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

        // Trends
        attendanceTrend.getData().clear();
        XYChart.Series<String, Number> attSeries = new XYChart.Series<>();
        attSeries.setName("Attendance %");
        AnalyticsService.weeklyAttendancePercent(c.getId(), 8).forEach((wk, pct) -> {
            attSeries.getData().add(new XYChart.Data<>(wk, pct));
        });
        attendanceTrend.getData().add(attSeries);

        examTrend.getData().clear();
        XYChart.Series<String, Number> exSeries = new XYChart.Series<>();
        exSeries.setName("Exam Avg %");
        AnalyticsService.weeklyExamAvgPercent(c.getId(), 8).forEach((wk, pct) -> {
            exSeries.getData().add(new XYChart.Data<>(wk, pct));
        });
        examTrend.getData().add(exSeries);

        installTooltips(dist, attSeries, exSeries);
    }

    private void installTooltips(Map<String, Integer> gradeDist,
                                 XYChart.Series<String, Number> attSeries,
                                 XYChart.Series<String, Number> exSeries) {
        // Pie tooltips for grade distribution
        int total = gradeDist.values().stream().mapToInt(Integer::intValue).sum();
        for (PieChart.Data d : gradePie.getData()) {
            String label = d.getName();
            int count = (int) d.getPieValue();
            String pct = total > 0 ? String.format("%.1f%%", (count * 100.0) / total) : "--";
            Tooltip tip = new Tooltip(label + ": " + count + " (" + pct + ")");
            if (d.getNode() != null) {
                Tooltip.install(d.getNode(), tip);
            } else {
                d.nodeProperty().addListener((obs, o, n) -> {
                    if (n != null) Tooltip.install(n, tip);
                });
            }
        }

        // Line chart tooltips
        for (XYChart.Data<String, Number> data : attSeries.getData()) {
            Tooltip tip = new Tooltip("Week " + data.getXValue() + ": " + data.getYValue() + "%");
            if (data.getNode() != null) Tooltip.install(data.getNode(), tip);
            else data.nodeProperty().addListener((obs, o, n) -> { if (n != null) Tooltip.install(n, tip); });
        }
        for (XYChart.Data<String, Number> data : exSeries.getData()) {
            Tooltip tip = new Tooltip("Week " + data.getXValue() + ": " + data.getYValue() + "%");
            if (data.getNode() != null) Tooltip.install(data.getNode(), tip);
            else data.nodeProperty().addListener((obs, o, n) -> { if (n != null) Tooltip.install(n, tip); });
        }
    }

    @FXML
    private void handleExportCsv() {
        Classroom c = classroomCombo.getValue();
        if (c == null) return;

        // Gather data in stable order
        Map<String, Double> courseAvgs = AnalyticsService.courseExamAverages(c.getId());
        Map<String, Double> attTrend = new LinkedHashMap<>(AnalyticsService.weeklyAttendancePercent(c.getId(), 8));
        Map<String, Double> examTrendMap = new LinkedHashMap<>(AnalyticsService.weeklyExamAvgPercent(c.getId(), 8));

        FileChooser fc = new FileChooser();
        fc.setTitle("Export Analytics CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName("analytics-" + c.getName().replaceAll("\\s+", "_") + ".csv");
        java.io.File file = fc.showSaveDialog(classroomCombo.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8)) {
            int upcomingDays = 14;
            out.println("Classroom," + escape(c.getName()));
            out.println("Upcoming Exams (" + upcomingDays + "d)," + AnalyticsService.countUpcomingExams(c.getId(), upcomingDays));
            out.println("Upcoming CT/Quiz (" + upcomingDays + "d)," + AnalyticsService.countUpcomingCTs(c.getId(), upcomingDays));
            out.println("Upcoming Labs (" + upcomingDays + "d)," + AnalyticsService.countUpcomingLabs(c.getId(), upcomingDays));
            Double ex = AnalyticsService.averageExamPercent(c.getId());
            Double ct = AnalyticsService.averageCTPercent(c.getId());
            Double lab = AnalyticsService.averageLabPercent(c.getId());
            Double att = AnalyticsService.overallAttendancePercent(c.getId());
            out.println("Avg Exam %," + (ex == null ? "--" : String.format("%.1f", ex)));
            out.println("Avg CT %," + (ct == null ? "--" : String.format("%.1f", ct)));
            out.println("Avg Lab %," + (lab == null ? "--" : String.format("%.1f", lab)));
            out.println("Overall Attendance %," + (att == null ? "--" : String.format("%.1f", att)));
            out.println();

            out.println("Per-Course Exam Averages");
            out.println("Course,Average %");
            courseAvgs.forEach((course, avg) -> out.println(escape(course) + "," + String.format("%.1f", avg)));
            out.println();

            out.println("Weekly Attendance % (last 8 weeks)");
            out.println("Week,Percent");
            attTrend.forEach((wk, pct) -> out.println(wk + "," + String.format("%.1f", pct)));
            out.println();

            out.println("Weekly Exam Avg % (last 8 weeks)");
            out.println("Week,Percent");
            examTrendMap.forEach((wk, pct) -> out.println(wk + "," + String.format("%.1f", pct)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
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
