package com.codeforces.analyzer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.codeforces.analyzer.database.DatabaseConnection;
import com.codeforces.analyzer.model.CrawlLog;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class CrawlLogDAO {
    public CrawlLog save(CrawlLog log) throws SQLException {
        String sql = "INSERT INTO crawl_logs(user_id, crawl_status, crawl_message, crawl_time) VALUES(?, ?, ?, NOW())";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() == null) {
                statement.setNull(1, java.sql.Types.BIGINT);
            } else {
                statement.setLong(1, log.getUserId());
            }
            statement.setString(2, log.getCrawlStatus());
            statement.setString(3, log.getCrawlMessage());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    log.setId(resultSet.getLong(1));
                }
            }
            return log;
        }
    }

    public List<CrawlLog> findRecent(int limit) throws SQLException {
        String sql = "SELECT * FROM crawl_logs ORDER BY crawl_time DESC LIMIT ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CrawlLog> logs = new ArrayList<>();
                while (resultSet.next()) {
                    CrawlLog log = new CrawlLog();
                    log.setId(resultSet.getLong("id"));
                    long userId = resultSet.getLong("user_id");
                    log.setUserId(resultSet.wasNull() ? null : userId);
                    log.setCrawlStatus(resultSet.getString("crawl_status"));
                    log.setCrawlMessage(resultSet.getString("crawl_message"));
                    log.setCrawlTime(DateTimeUtils.fromTimestamp(resultSet.getTimestamp("crawl_time")));
                    logs.add(log);
                }
                return logs;
            }
        }
    }
}
