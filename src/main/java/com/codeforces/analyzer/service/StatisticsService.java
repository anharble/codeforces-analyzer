package com.codeforces.analyzer.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.codeforces.analyzer.dao.AnalysisDAO;
import com.codeforces.analyzer.dao.SubmissionDAO;
import com.codeforces.analyzer.dao.UserDAO;
import com.codeforces.analyzer.model.AnalysisResult;
import com.codeforces.analyzer.model.DashboardStats;
import com.codeforces.analyzer.model.Submission;
import com.codeforces.analyzer.model.UserEvaluation;
import com.codeforces.analyzer.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class StatisticsService {
    private final UserDAO userDAO;
    private final SubmissionDAO submissionDAO;
    private final AnalysisDAO analysisDAO;

    public StatisticsService(UserDAO userDAO, SubmissionDAO submissionDAO, AnalysisDAO analysisDAO) {
        this.userDAO = userDAO;
        this.submissionDAO = submissionDAO;
        this.analysisDAO = analysisDAO;
    }

    public DashboardStats getDashboardStats() {
        try {
            DashboardStats stats = new DashboardStats();
            stats.setTotalUsers(userDAO.countAll());
            stats.setTotalSubmissions(submissionDAO.countAll());
            stats.setAiUsageRate(analysisDAO.findAverageAiProbability());

            List<AnalysisResult> results = analysisDAO.findAll();
            stats.setTopAlgorithms(topFromResults(results, true, 10));
            stats.setTopDataStructures(topFromResults(results, false, 10));
            stats.setTopUsers(submissionDAO.findTopUsersBySubmission(10));
            return stats;
        } catch (SQLException e) {
            throw new IllegalStateException("Không tính được thống kê tổng quan.", e);
        }
    }

    public UserEvaluation evaluateUser(long userId) {
        try {
            List<Submission> submissions = submissionDAO.findByUser(userId);
            List<AnalysisResult> results = new ArrayList<>();
            for (Submission submission : submissions) {
                AnalysisResult result = analysisDAO.findBySubmissionId(submission.getId());
                if (result != null) {
                    results.add(result);
                }
            }

            long total = submissions.size();
            long accepted = submissionDAO.countAcceptedByUser(userId);
            double acceptedRate = total == 0 ? 0 : accepted * 100.0 / total;

            int dsScore = average(results.stream().map(AnalysisResult::getDataStructureScore).toList());
            int algorithmScore = average(results.stream().map(AnalysisResult::getAlgorithmScore).toList());
            int aiScore = average(results.stream().map(AnalysisResult::getAiProbability).toList());
            int problemSolvingScore = (int) Math.round(Math.min(100, acceptedRate * 0.65 + Math.min(total, 120) * 0.35));

            int overall = (int) Math.round(algorithmScore * 0.35 + dsScore * 0.25
                    + problemSolvingScore * 0.30 + (100 - aiScore) * 0.10);

            UserEvaluation evaluation = new UserEvaluation();
            evaluation.setDataStructureScore(dsScore);
            evaluation.setAlgorithmScore(algorithmScore);
            evaluation.setProblemSolvingScore(problemSolvingScore);
            evaluation.setAiUsageScore(aiScore);
            evaluation.setAcceptedRate(acceptedRate);
            evaluation.setCommonLanguage(submissionDAO.findMostUsedLanguage(userId));
            evaluation.setRankName(rankName(overall));
            evaluation.setSummary(buildSummary(evaluation, total, accepted));
            return evaluation;
        } catch (SQLException e) {
            throw new IllegalStateException("Không đánh giá được nick.", e);
        }
    }

    private String buildSummary(UserEvaluation evaluation, long total, long accepted) {
        return "Tổng bài nộp: " + total
                + "\nSố bài được chấp nhận: " + accepted
                + "\nTỉ lệ được chấp nhận: " + String.format("%.2f%%", evaluation.getAcceptedRate())
                + "\nNgôn ngữ thường dùng: " + evaluation.getCommonLanguage()
                + "\nXếp loại: " + evaluation.getRankName()
                + "\nMức nghi ngờ AI trung bình: " + evaluation.getAiUsageScore() + "%";
    }

    private String rankName(int score) {
        if (score < 35) {
            return "Mới bắt đầu";
        }
        if (score < 60) {
            return "Trung cấp";
        }
        if (score < 80) {
            return "Nâng cao";
        }
        return "Lập trình thi đấu";
    }

    private int average(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        return (int) Math.round(values.stream().mapToInt(Integer::intValue).average().orElse(0));
    }

    private Map<String, Integer> topFromResults(List<AnalysisResult> results, boolean algorithms, int limit) {
        Map<String, Integer> counter = new HashMap<>();
        for (AnalysisResult result : results) {
            String raw = algorithms ? result.getDetectedAlgorithms() : result.getDetectedDataStructures();
            for (String item : readItems(raw)) {
                if (!item.isBlank()) {
                    counter.merge(item.trim(), 1, Integer::sum);
                }
            }
        }
        return counter.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry::getKey))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public List<String> readItems(String raw) {
        List<String> items = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return items;
        }
        try {
            JsonElement element = JsonParser.parseString(raw);
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (JsonElement child : array) {
                    items.add(child.getAsString());
                }
                return items;
            }
        } catch (Exception ignored) {
            // Chuỗi cũ có thể được lưu dạng phân tách bằng dấu phẩy.
        }
        String[] parts = raw.replace("[", "").replace("]", "").replace("\"", "").split(",");
        for (String part : parts) {
            String item = part.trim();
            if (!item.isBlank()) {
                items.add(item);
            }
        }
        return items;
    }

    public String toJsonList(List<String> values) {
        return JsonUtils.gson().toJson(values == null ? List.of() : values);
    }
}
