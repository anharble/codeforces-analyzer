package com.codeforces.analyzer.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.codeforces.analyzer.model.AnalysisResult;
import com.codeforces.analyzer.model.Submission;
import com.codeforces.analyzer.utils.JsonUtils;

public class HeuristicAnalyzer {
    public AnalysisResult analyze(Submission submission) {
        String code = submission.getSourceCode() == null ? "" : submission.getSourceCode();
        String lower = code.toLowerCase(Locale.ROOT);

        List<String> dataStructures = detectDataStructures(lower);
        List<String> algorithms = detectAlgorithms(lower);

        int aiProbability = estimateAiProbability(code, lower);
        int algorithmScore = Math.min(100, algorithms.size() * 16 + (lower.contains("dp") ? 12 : 0));
        int dsScore = Math.min(100, dataStructures.size() * 14);

        AnalysisResult result = new AnalysisResult();
        result.setSubmissionId(submission.getId());
        result.setDetectedAlgorithms(JsonUtils.gson().toJson(algorithms));
        result.setDetectedDataStructures(JsonUtils.gson().toJson(dataStructures));
        result.setDifficultyLevel(estimateDifficulty(algorithms, dataStructures));
        result.setAiProbability(aiProbability);
        result.setAiConfidence(aiProbability >= 70 ? "Cao" : aiProbability >= 45 ? "Trung bình" : "Thấp");
        result.setAiReasons(JsonUtils.gson().toJson(List.of(
                "Đây là phân tích dự phòng khi chưa dùng được Groq API.",
                "Kết quả dựa trên từ khóa, cấu trúc code và mức đồng đều định dạng."
        )));
        result.setAiComment("Phân tích dự phòng chỉ dùng để tham khảo. Khi cấu hình khóa Groq API, hệ thống sẽ phân tích sâu hơn về thuật toán, cấu trúc dữ liệu và dấu hiệu mã nguồn do AI sinh.");
        result.setAlgorithmScore(algorithmScore);
        result.setDataStructureScore(dsScore);
        return result;
    }

    private List<String> detectDataStructures(String lower) {
        List<String> items = new ArrayList<>();
        addIf(items, lower.contains("vector<") || lower.contains("array"), "Mảng hoặc Vector");
        addIf(items, lower.contains("stack<"), "Ngăn xếp");
        addIf(items, lower.contains("queue<"), "Hàng đợi");
        addIf(items, lower.contains("deque<"), "Hàng đợi hai đầu");
        addIf(items, lower.contains("set<") || lower.contains("unordered_set"), "Tập hợp");
        addIf(items, lower.contains("map<") || lower.contains("unordered_map"), "Bảng ánh xạ");
        addIf(items, lower.contains("priority_queue"), "Hàng đợi ưu tiên");
        addIf(items, lower.contains("parent") && (lower.contains("find(") || lower.contains("union")), "DSU");
        addIf(items, lower.contains("seg") || lower.contains("segment"), "Cây phân đoạn");
        addIf(items, lower.contains("fenwick") || lower.contains("bit["), "Cây Fenwick");
        addIf(items, lower.contains("trie"), "Trie");
        addIf(items, lower.contains("adj") || lower.contains("graph"), "Đồ thị");
        if (items.isEmpty()) {
            items.add("Cấu trúc cơ bản");
        }
        return items;
    }

    private List<String> detectAlgorithms(String lower) {
        List<String> items = new ArrayList<>();
        addIf(items, lower.contains("sort("), "Sắp xếp");
        addIf(items, lower.contains("lower_bound") || lower.contains("upper_bound") || lower.contains("binary_search"), "Tìm kiếm nhị phân");
        addIf(items, lower.contains("dfs("), "DFS");
        addIf(items, lower.contains("bfs("), "BFS");
        addIf(items, lower.contains("dijkstra"), "Dijkstra");
        addIf(items, lower.contains("floyd"), "Floyd Warshall");
        addIf(items, lower.contains("dp[") || lower.contains("memo"), "Quy hoạch động");
        addIf(items, lower.contains("backtrack"), "Quay lui");
        addIf(items, lower.contains("two") && lower.contains("pointer"), "Hai con trỏ");
        addIf(items, lower.contains("mask") || lower.contains("bitmask"), "Bitmask");
        addIf(items, lower.contains("topo"), "Sắp xếp topo");
        addIf(items, lower.contains("kruskal") || lower.contains("prim("), "Cây khung nhỏ nhất");
        addIf(items, lower.contains("kmp") || lower.contains("prefix_function"), "So khớp chuỗi");
        if (items.isEmpty()) {
            items.add("Duyệt và rẽ nhánh cơ bản");
        }
        return items;
    }

    private void addIf(List<String> items, boolean condition, String value) {
        if (condition && !items.contains(value)) {
            items.add(value);
        }
    }

    private int estimateAiProbability(String code, String lower) {
        int score = 20;
        if (code.lines().filter(line -> line.trim().startsWith("//")).count() >= 4) {
            score += 15;
        }
        if (lower.contains("solve()") && lower.contains("ios::sync_with_stdio(false)")) {
            score += 10;
        }
        if (lower.contains("long long") && lower.contains("const int") && lower.contains("mod")) {
            score += 10;
        }
        if (code.length() > 2500 && code.lines().map(String::trim).filter(s -> !s.isBlank()).distinct().count() > 40) {
            score += 10;
        }
        if (!lower.contains("//") && code.length() < 1200) {
            score -= 10;
        }
        return Math.max(0, Math.min(100, score));
    }

    private String estimateDifficulty(List<String> algorithms, List<String> dataStructures) {
        int weight = algorithms.size() * 2 + dataStructures.size();
        if (weight <= 4) {
            return "Cơ bản";
        }
        if (weight <= 8) {
            return "Trung cấp";
        }
        if (weight <= 12) {
            return "Nâng cao";
        }
        return "Chuyên gia";
    }
}
