package com.codeforces.analyzer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.codeforces.analyzer.database.DatabaseConnection;
import com.codeforces.analyzer.model.Submission;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class SubmissionDAO {
    public boolean saveIfNew(Submission submission) throws SQLException {
        if (exists(submission.getUserId(), submission.getSubmissionId())) {
            return false;
        }
        String sql = """
                INSERT INTO submissions(
                    user_id, submission_id, problem_name, verdict, language, submit_time,
                    source_code, source_url, source_crawl_status, source_crawl_message, crawl_time
                ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillSubmissionStatement(statement, submission);
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    submission.setId(resultSet.getLong(1));
                }
            }
            return true;
        }
    }

    public void updateSource(long submissionDbId, String sourceCode, String status, String message) throws SQLException {
        String sql = """
                UPDATE submissions
                SET source_code = ?, source_crawl_status = ?, source_crawl_message = ?, crawl_time = NOW()
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sourceCode);
            statement.setString(2, status);
            statement.setString(3, message);
            statement.setLong(4, submissionDbId);
            statement.executeUpdate();
        }
    }

    public void updateMetadata(Submission submission) throws SQLException {
        String sql = """
                UPDATE submissions
                SET problem_name = ?, verdict = ?, language = ?, submit_time = ?, source_url = ?, crawl_time = NOW()
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, submission.getProblemName());
            statement.setString(2, submission.getVerdict());
            statement.setString(3, submission.getLanguage());
            statement.setTimestamp(4, DateTimeUtils.toTimestamp(submission.getSubmitTime()));
            statement.setString(5, submission.getSourceUrl());
            statement.setLong(6, submission.getId());
            statement.executeUpdate();
        }
    }

    public boolean exists(long userId, String submissionId) throws SQLException {
        String sql = "SELECT 1 FROM submissions WHERE user_id = ? AND submission_id = ? LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, submissionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public Submission findByUserAndSubmissionId(long userId, String submissionId) throws SQLException {
        String sql = "SELECT * FROM submissions WHERE user_id = ? AND submission_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, submissionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapSubmission(resultSet);
                }
                return null;
            }
        }
    }

    public Submission findById(long id) throws SQLException {
        String sql = "SELECT * FROM submissions WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapSubmission(resultSet);
                }
                return null;
            }
        }
    }

    public List<Submission> findByUser(long userId) throws SQLException {
        String sql = "SELECT * FROM submissions WHERE user_id = ? ORDER BY submit_time DESC, id DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Submission> submissions = new ArrayList<>();
                while (resultSet.next()) {
                    submissions.add(mapSubmission(resultSet));
                }
                return submissions;
            }
        }
    }

    public List<Submission> findAll() throws SQLException {
        String sql = "SELECT * FROM submissions ORDER BY submit_time DESC, id DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Submission> submissions = new ArrayList<>();
            while (resultSet.next()) {
                submissions.add(mapSubmission(resultSet));
            }
            return submissions;
        }
    }

    public List<Submission> findWithoutAnalysis() throws SQLException {
        String sql = """
                SELECT s.* FROM submissions s
                LEFT JOIN analysis_results ar ON ar.submission_id = s.id
                WHERE ar.id IS NULL
                  AND s.source_code IS NOT NULL
                  AND LENGTH(TRIM(s.source_code)) > 0
                ORDER BY s.submit_time DESC, s.id DESC
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Submission> submissions = new ArrayList<>();
            while (resultSet.next()) {
                submissions.add(mapSubmission(resultSet));
            }
            return submissions;
        }
    }

    public long countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM submissions";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    public long countByUser(long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM submissions WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public long countAcceptedByUser(long userId) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM submissions
                WHERE user_id = ?
                  AND (UPPER(verdict) = 'OK' OR LOWER(verdict) LIKE '%accepted%' OR verdict LIKE '%Chấp nhận%')
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    public String findMostUsedLanguage(long userId) throws SQLException {
        String sql = """
                SELECT language, COUNT(*) AS total FROM submissions
                WHERE user_id = ? AND language IS NOT NULL AND language <> ''
                GROUP BY language
                ORDER BY total DESC
                LIMIT 1
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("language");
                }
                return "Chưa có dữ liệu";
            }
        }
    }

    public Map<String, Integer> findTopUsersBySubmission(int limit) throws SQLException {
        String sql = """
                SELECT u.handle, COUNT(s.id) AS total
                FROM users u
                LEFT JOIN submissions s ON s.user_id = u.id
                GROUP BY u.id, u.handle
                ORDER BY total DESC, u.handle
                LIMIT ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<String, Integer> result = new LinkedHashMap<>();
                while (resultSet.next()) {
                    result.put(resultSet.getString("handle"), resultSet.getInt("total"));
                }
                return result;
            }
        }
    }

    private void fillSubmissionStatement(PreparedStatement statement, Submission submission) throws SQLException {
        statement.setLong(1, submission.getUserId());
        statement.setString(2, submission.getSubmissionId());
        statement.setString(3, submission.getProblemName());
        statement.setString(4, submission.getVerdict());
        statement.setString(5, submission.getLanguage());
        statement.setTimestamp(6, DateTimeUtils.toTimestamp(submission.getSubmitTime()));
        statement.setString(7, submission.getSourceCode());
        statement.setString(8, submission.getSourceUrl());
        statement.setString(9, submission.getSourceCrawlStatus());
        statement.setString(10, submission.getSourceCrawlMessage());
    }

    private Submission mapSubmission(ResultSet resultSet) throws SQLException {
        Submission submission = new Submission();
        submission.setId(resultSet.getLong("id"));
        submission.setUserId(resultSet.getLong("user_id"));
        submission.setSubmissionId(resultSet.getString("submission_id"));
        submission.setProblemName(resultSet.getString("problem_name"));
        submission.setVerdict(resultSet.getString("verdict"));
        submission.setLanguage(resultSet.getString("language"));
        submission.setSubmitTime(DateTimeUtils.fromTimestamp(resultSet.getTimestamp("submit_time")));
        submission.setSourceCode(resultSet.getString("source_code"));
        submission.setSourceUrl(resultSet.getString("source_url"));
        submission.setSourceCrawlStatus(resultSet.getString("source_crawl_status"));
        submission.setSourceCrawlMessage(resultSet.getString("source_crawl_message"));
        submission.setCrawlTime(DateTimeUtils.fromTimestamp(resultSet.getTimestamp("crawl_time")));
        return submission;
    }
}
