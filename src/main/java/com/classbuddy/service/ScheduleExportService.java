package com.classbuddy.service;

import com.classbuddy.model.CTQuiz;
import com.classbuddy.model.Exam;
import com.classbuddy.model.LabTest;
import com.classbuddy.model.Notice;
import com.classbuddy.model.Routine;
import com.classbuddy.util.DateFormats;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for exporting classroom schedules to CSV format
 */
public class ScheduleExportService {


    /**
     * Exports entire classroom schedule to CSV file
     * 
     * @param classroomId The classroom to export
     * @param outputPath Path where CSV file will be written
     * @return ExportResult with statistics
     * @throws IOException if file writing fails
     */
    public static ExportResult exportScheduleToCSV(int classroomId, Path outputPath) throws IOException {
        int totalRows = 0;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath.toFile()))) {
            // Write header
            writer.println("# ClassBuddy Schedule Export - Format Version 1.0");
            writer.println("# Generated: " + LocalDate.now());
            writer.println("Type,Day/Date,Period,Course,Teacher,Room,StartTime,EndTime,Metadata");
            
            // Export routines
            List<Routine> routines = RoutineService.getWeeklyRoutine(classroomId);
            for (Routine routine : routines) {
                // Expand multi-day routines into individual rows
                for (String day : routine.getApplicableDays()) {
                    writer.println(formatRoutineRow(routine, day));
                    totalRows++;
                }
            }
            
            // Export exams
            List<Exam> exams = ExamService.getClassroomExams(classroomId);
            for (Exam exam : exams) {
                writer.println(formatExamRow(exam));
                totalRows++;
            }
            
            // Export CT quizzes
            List<CTQuiz> ctQuizzes = CTQuizService.getClassroomCTQuizzes(classroomId);
            for (CTQuiz ct : ctQuizzes) {
                writer.println(formatCTQuizRow(ct));
                totalRows++;
            }
            
            // Export lab tests
            List<LabTest> labTests = LabTestService.getClassroomLabTests(classroomId);
            for (LabTest lab : labTests) {
                writer.println(formatLabTestRow(lab));
                totalRows++;
            }
            
            // Export notices
            List<Notice> notices = NoticeService.getClassroomNotices(classroomId);
            for (Notice notice : notices) {
                writer.println(formatNoticeRow(notice));
                totalRows++;
            }
        }
        
        return new ExportResult(true, totalRows, outputPath.toString());
    }

    private static String formatRoutineRow(Routine routine, String day) {
        return String.format("ROUTINE,%s,%d,%s,%s,%s,%s,%s,",
            escapeCsv(day),
            routine.getPeriodNumber(),
            escapeCsv(routine.getCourseName()),
            escapeCsv(routine.getTeacherName()),
            escapeCsv(routine.getRoom()),
            DateFormats.time(routine.getTimeStart()),
            DateFormats.time(routine.getTimeEnd())
        );
    }

    private static String formatExamRow(Exam exam) {
        String metadata = "exam_type=" + exam.getExamType();
        return String.format("EXAM,%s,,%s,,%s,%s,,%s",
            DateFormats.dateIso(exam.getExamDate()),
            escapeCsv(exam.getCourseName()),
            escapeCsv(exam.getRoom()),
            DateFormats.time(exam.getExamTime()),
            escapeCsv(metadata)
        );
    }

    private static String formatCTQuizRow(CTQuiz ct) {
        String metadata = "name=" + ct.getName();
        return String.format("CT,%s,,%s,,,,,%s",
            DateFormats.dateIso(ct.getDeadline()),
            escapeCsv(ct.getSyllabus()),
            escapeCsv(metadata)
        );
    }

    private static String formatLabTestRow(LabTest lab) {
        String metadata = "experiment=" + lab.getExperimentNumber() + ";teacher=" + lab.getTeacherName();
        return String.format("LAB,%s,,,,,,,%s",
            DateFormats.dateIso(lab.getTestDate()),
            escapeCsv(metadata)
        );
    }

    private static String formatNoticeRow(Notice notice) {
        String metadata = String.format("category=%s;pinned=%b",
            notice.getCategory(), notice.isPinned());
        return String.format("NOTICE,%s,,%s,,,,,%s",
            DateFormats.dateIso(notice.getCreatedAt().toLocalDate()),
            escapeCsv(notice.getTitle()),
            escapeCsv(metadata)
        );
    }

    /**
     * Escapes CSV values containing commas or quotes
     */
    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Result object for export operations
     */
    public static class ExportResult {
        private final boolean success;
        private final int totalRows;
        private final String filePath;

        public ExportResult(boolean success, int totalRows, String filePath) {
            this.success = success;
            this.totalRows = totalRows;
            this.filePath = filePath;
        }

        public boolean isSuccess() { return success; }
        public int getTotalRows() { return totalRows; }
        public String getFilePath() { return filePath; }

        @Override
        public String toString() {
            return String.format("Export %s: %d rows written to %s",
                success ? "SUCCESS" : "FAILED", totalRows, filePath);
        }
    }
}
