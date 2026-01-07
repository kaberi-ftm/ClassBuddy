package com.classbuddy.util;

import java.time.LocalDate;

public class ClassIdGenerator {

    public static String generate(String classroomName, String section) {
        int year = LocalDate.now().getYear();
        String subject = normalize(classroomName);
        String sec = normalize(section);

        if (subject.isEmpty()) subject = "CLASS";
        if (sec.isEmpty()) sec = "A";

        return subject + "-" + sec + "-" + year;
    }

    private static String normalize(String s) {
        if (s == null) return "";

        String out = s.trim().toUpperCase();
        out = out.replaceAll("[^A-Z0-9]+", "");

        return out;
    }
}
