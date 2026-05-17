package com.codeforces.analyzer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.codeforces.analyzer.database.DatabaseConnection;
import com.codeforces.analyzer.model.AnalysisResult;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class AnalysisDAO {
    public AnalysisResult saveOrUpdate(AnalysisResult result) throws SQLException {
        AnalysisResult old = findBySubmissionId(result.getSubmissionId());
        if (old == null) {
            return save(result);
        }
        result.setId(old.getId());
        update(result);
        return result;
    }

    public AnalysisResult save(AnalysisResult result) throws SQLException {
        String sql = """
                INSERT INTO analysis_results(
                    submission_id, detected_algorithms, detected_data_structures, difficulty_level,
                    ai_probability, ai_confidence, ai_reasons, ai_comment,
                    data_structure_score, algorithm_score, analysis_time
                ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, result);
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    result.setId(resultSet.getLong(1));
                }
            }
            return result;
        }
    }

    public void update(AnalysisResult result) throws SQLException {
        String sql = """
                UPDATE analysis_results
                SET detected_algorithms = ?, detected_data_structures = ?, difficulty_level = ?,
                    ai_probability = ?, ai_confidence = ?, ai_reasons = ?, ai_comment = ?,
                    data_structure_score = ?, algorithm_score = ?, analysis_time = NOW()
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, result.getDetectedAlgorithms());
            statement.setString(2, result.getDetectedDataStructures());
            statement.setString(3, result.getDifficultyLevel());
            statement.setInt(4, result.getAiProbability());
            statement.setString(5, result.getAiConfidence());
            statement.setString(6, result.getAiReasons());
            statement.setString(7, result.getAiComment());
            statement.setInt(8, result.getDataStructureScore());
            statement.setInt(9, result.getAlgorithmScore());
            statement.setLong(10, result.getId());
            statement.executeUpdate();
        }
    }

    public AnalysisResult findBySubmissionId(long submissionId) throws SQLException {
        String sql = "SELECT * FROM analysis_results WHERE submission_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, submissionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResult(resultSet);
                }
                return null;
            }
        }
    }

    public List<AnalysisResult> findAll() throws SQLException {
        String sql = "SELECT * FROM analysis_results ORDER BY analysis_time DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<AnalysisResult> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapResult(resultSet));
            }
            return results;
        }
    }

    public double findAverageAiProbability() throws SQLException {
        String sql = "SELECT COALESCE(AVG(ai_probability), 0) FROM analysis_results";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getDouble(1);
        }
    }

    private void fillStatement(PreparedStatement statement, AnalysisResult result) throws SQLException {
        statement.setLong(1, result.getSubmissionId());
        statement.setString(2, result.getDetectedAlgorithms());
        statement.setString(3, result.getDetectedDataStructures());
        statement.setString(4, result.getDifficultyLevel());
        statement.setInt(5, result.getAiProbability());
        statement.setString(6, result.getAiConfidence());
        statement.setString(7, result.getAiReasons());
        statement.setString(8, result.getAiComment());
        statement.setInt(9, result.getDataStructureScore());
        statement.setInt(10, result.getAlgorithmScore());
    }

    private AnalysisResult mapResult(ResultSet resultSet) throws SQLException {
        AnalysisResult result = new AnalysisResult();
        result.setId(resultSet.getLong("id"));
        result.setSubmissionId(resultSet.getLong("submission_id"));
        result.setDetectedAlgorithms(resultSet.getString("detected_algorithms"));
        result.setDetectedDataStructures(resultSet.getString("detected_data_structures"));
        result.setDifficultyLevel(resultSet.getString("difficulty_level"));
        result.setAiProbability(resultSet.getInt("ai_probability"));
        result.setAiConfidence(resultSet.getString("ai_confidence"));
        result.setAiReasons(resultSet.getString("ai_reasons"));
        result.setAiComment(resultSet.getString("ai_comment"));
        result.setDataStructureScore(resultSet.getInt("data_structure_score"));
        result.setAlgorithmScore(resultSet.getInt("algorithm_score"));
        result.setAnalysisTime(DateTimeUtils.fromTimestamp(resultSet.getTimestamp("analysis_time")));
        return result;
    }
}
