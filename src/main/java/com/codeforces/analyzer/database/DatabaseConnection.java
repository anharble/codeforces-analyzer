package com.codeforces.analyzer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeforces.analyzer.utils.AppConfig;

public final class DatabaseConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy trình điều khiển MySQL.", e);
        }

        String url = AppConfig.get("db.url");
        String username = AppConfig.get("db.username");
        String password = AppConfig.get("db.password");
        if (url == null || url.isBlank()) {
            throw new SQLException("Chưa cấu hình đường dẫn kết nối cơ sở dữ liệu.");
        }
        return DriverManager.getConnection(url, username, password);
    }

    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            boolean valid = connection.isValid(5);
            if (valid) {
                LOGGER.info("Kết nối cơ sở dữ liệu thành công.");
            }
            return valid;
        } catch (SQLException e) {
            LOGGER.error("Kết nối cơ sở dữ liệu thất bại: {}", e.getMessage());
            return false;
        }
    }
}
