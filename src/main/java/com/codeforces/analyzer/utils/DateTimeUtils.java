package com.codeforces.analyzer.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class DateTimeUtils {
    private static final DateTimeFormatter HIEN_THI = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter CODEFORCES = DateTimeFormatter.ofPattern("MMM/dd/yyyy HH:mm", Locale.ENGLISH);

    private DateTimeUtils() {
    }

    public static Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    public static LocalDateTime fromTimestamp(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    public static String format(LocalDateTime value) {
        return value == null ? "" : HIEN_THI.format(value);
    }

    public static LocalDateTime parseCodeforcesTime(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }
        String normalized = rawText.replaceAll("\\s+", " ").trim();
        normalized = normalized.replace("UTC", "").replaceAll("[+-]\\d+", "").trim();
        try {
            return LocalDateTime.parse(normalized, CODEFORCES);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
