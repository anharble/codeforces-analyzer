package com.codeforces.analyzer.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties PROPERTIES = new Properties();

    static {
        taiCauHinh();
    }

    private AppConfig() {
    }

    private static void taiCauHinh() {
        try (InputStream inputStream = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (inputStream != null) {
                PROPERTIES.load(inputStream);
            }
        } catch (IOException e) {
            LOGGER.warn("Không đọc được cấu hình mặc định: {}", e.getMessage());
        }

        Path externalConfig = Path.of("config", "application.properties");
        if (Files.exists(externalConfig)) {
            try (InputStream inputStream = Files.newInputStream(externalConfig)) {
                PROPERTIES.load(inputStream);
                LOGGER.info("Đã nạp cấu hình bên ngoài từ {}", externalConfig.toAbsolutePath());
            } catch (IOException e) {
                LOGGER.warn("Không đọc được cấu hình bên ngoài: {}", e.getMessage());
            }
        }
    }

    public static String get(String key) {
        return System.getProperty(key, PROPERTIES.getProperty(key, ""));
    }

    public static String get(String key, String defaultValue) {
        return System.getProperty(key, PROPERTIES.getProperty(key, defaultValue));
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            LOGGER.warn("Giá trị cấu hình {} không phải số hợp lệ, dùng mặc định {}", key, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }
}
