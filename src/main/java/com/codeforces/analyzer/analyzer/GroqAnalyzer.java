package com.codeforces.analyzer.analyzer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeforces.analyzer.model.AnalysisResult;
import com.codeforces.analyzer.model.Submission;
import com.codeforces.analyzer.utils.AppConfig;
import com.codeforces.analyzer.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GroqAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroqAnalyzer.class);

    private final GroqService groqService;
    private final HeuristicAnalyzer heuristicAnalyzer;

    public GroqAnalyzer(GroqService groqService, HeuristicAnalyzer heuristicAnalyzer) {
        this.groqService = groqService;
        this.heuristicAnalyzer = heuristicAnalyzer;
    }

    public AnalysisResult analyze(Submission submission) {
        if (submission == null || submission.getId() <= 0) {
            throw new IllegalArgumentException("Bài nộp không hợp lệ.");
        }
        if (submission.getSourceCode() == null || submission.getSourceCode().isBlank()) {
            throw new IllegalArgumentException("Bài nộp chưa có mã nguồn để phân tích.");
        }

        try {
            String response = groqService.chatJson(buildPrompt(submission));
            return parseResponse(submission.getId(), response);
        } catch (Exception e) {
            if (AppConfig.getBoolean("groq.fallback.enabled", true)) {
                LOGGER.warn("Không phân tích được bằng Groq, chuyển sang phân tích dự phòng: {}", e.getMessage());
                return heuristicAnalyzer.analyze(submission);
            }
            throw e;
        }
    }

    private String buildPrompt(Submission submission) {
        String code = submission.getSourceCode();
        int maxCodeLength = AppConfig.getInt("groq.max.code.characters", 6000);
        if (code.length() > maxCodeLength) {
            code = code.substring(0, maxCodeLength) + "\n/* Đã cắt bớt phần cuối vì mã nguồn quá dài. */";
        }
        return """
                Hãy phân tích mã nguồn thi đấu lập trình dưới đây bằng tiếng Việt.

                Yêu cầu bắt buộc:
                - Nhận diện cấu trúc dữ liệu.
                - Nhận diện thuật toán.
                - Đánh giá độ khó tư duy thuật toán.
                - Ước lượng xác suất code có thể do AI hỗ trợ.
                - Không kết luận tuyệt đối nếu thiếu bằng chứng.
                - Trả về đúng một JSON hợp lệ, không markdown, không giải thích ngoài JSON.

                Mẫu JSON bắt buộc:
                {
                  "thuat_toan": ["Sắp xếp"],
                  "cau_truc_du_lieu": ["Mảng"],
                  "muc_do": "Cơ bản",
                  "xac_suat_ai": 35,
                  "do_tin_cay": "Trung bình",
                  "ly_do_ai": ["Lý do ngắn gọn"],
                  "nhan_xet": "Nhận xét chi tiết bằng tiếng Việt",
                  "diem_ctdl": 40,
                  "diem_thuat_toan": 45
                }

                Các mức độ hợp lệ: "Mới bắt đầu", "Cơ bản", "Trung cấp", "Nâng cao", "Chuyên gia".
                Độ tin cậy hợp lệ: "Thấp", "Trung bình", "Cao".
                Điểm nằm trong khoảng 0 đến 100.

                Thông tin bài nộp:
                - Mã bài nộp: %s
                - Bài làm: %s
                - Ngôn ngữ: %s
                - Kết quả: %s

                Source code:
                ```text
                %s
                ```
                """.formatted(
                safe(submission.getSubmissionId()),
                safe(submission.getProblemName()),
                safe(submission.getLanguage()),
                safe(submission.getVerdict()),
                code
        );
    }

    private AnalysisResult parseResponse(long submissionDbId, String response) {
        String json = JsonUtils.extractJsonObject(response);
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();

        List<String> algorithms = readStringList(object, "thuat_toan");
        List<String> dataStructures = readStringList(object, "cau_truc_du_lieu");
        List<String> reasons = readStringList(object, "ly_do_ai");

        AnalysisResult result = new AnalysisResult();
        result.setSubmissionId(submissionDbId);
        result.setDetectedAlgorithms(JsonUtils.gson().toJson(algorithms));
        result.setDetectedDataStructures(JsonUtils.gson().toJson(dataStructures));
        result.setDifficultyLevel(readString(object, "muc_do", "Chưa rõ"));
        result.setAiProbability(clamp(readInt(object, "xac_suat_ai", 0)));
        result.setAiConfidence(readString(object, "do_tin_cay", "Thấp"));
        result.setAiReasons(JsonUtils.gson().toJson(reasons));
        result.setAiComment(readString(object, "nhan_xet", "Chưa có nhận xét."));
        result.setDataStructureScore(clamp(readInt(object, "diem_ctdl", 0)));
        result.setAlgorithmScore(clamp(readInt(object, "diem_thuat_toan", 0)));
        return result;
    }

    private List<String> readStringList(JsonObject object, String key) {
        List<String> values = new ArrayList<>();
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return values;
        }
        JsonElement element = object.get(key);
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) {
                values.add(child.getAsString());
            }
        } else {
            values.add(element.getAsString());
        }
        return values;
    }

    private String readString(JsonObject object, String key, String defaultValue) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return defaultValue;
        }
        return object.get(key).getAsString();
    }

    private int readInt(JsonObject object, String key, int defaultValue) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return defaultValue;
        }
        try {
            return object.get(key).getAsInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
