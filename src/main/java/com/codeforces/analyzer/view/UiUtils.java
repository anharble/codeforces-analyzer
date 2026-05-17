package com.codeforces.analyzer.view;

import java.awt.Component;

import javax.swing.JOptionPane;

public final class UiUtils {
    private UiUtils() {
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message, Throwable throwable) {
        String detail = throwable == null ? "" : "\nChi tiết: " + userMessage(throwable);
        JOptionPane.showMessageDialog(parent, message + detail, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    private static String userMessage(Throwable throwable) {
        Throwable current = throwable;
        String fallback = "Không có thông tin chi tiết.";
        while (current.getCause() != null) {
            String message = cleanMessage(current.getMessage());
            if (!message.isBlank()) {
                fallback = message;
                if (isFriendlyMessage(message)) {
                    return message;
                }
            }
            current = current.getCause();
        }
        String message = cleanMessage(current.getMessage());
        if (!message.isBlank() && isFriendlyMessage(message)) {
            return message;
        }
        return fallback;
    }

    private static boolean isFriendlyMessage(String message) {
        return !message.contains("Build info:")
                && !message.contains("For documentation")
                && !message.contains("Command:")
                && !message.contains("Session ID:")
                && !message.contains("Capabilities")
                && !message.contains("no such element:")
                && !message.contains("Expected condition failed:");
    }

    private static String cleanMessage(String message) {
        if (message == null) {
            return "";
        }
        String cleaned = message.trim();
        String[] prefixes = {
                "java.lang.IllegalStateException:",
                "java.util.concurrent.ExecutionException:",
                "org.openqa.selenium.TimeoutException:",
                "org.openqa.selenium.NoSuchElementException:"
        };
        boolean changed;
        do {
            changed = false;
            for (String prefix : prefixes) {
                if (cleaned.startsWith(prefix)) {
                    cleaned = cleaned.substring(prefix.length()).trim();
                    changed = true;
                }
            }
        } while (changed);
        int seleniumDetailIndex = cleaned.indexOf("\nBuild info:");
        if (seleniumDetailIndex >= 0) {
            cleaned = cleaned.substring(0, seleniumDetailIndex).trim();
        }
        return cleaned;
    }
}
