package com.codeforces.analyzer.analyzer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.codeforces.analyzer.utils.AppConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GroqService {
    private static final Object REQUEST_LOCK = new Object();
    private static long lastRequestTimeMillis = 0L;

    private final HttpClient httpClient;

    public GroqService() {
        int timeout = AppConfig.getInt("groq.timeout.seconds", 60);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .build();
    }

    public String chatJson(String userPrompt) {
        String apiKey = AppConfig.get("groq.api.key").trim();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình khóa Groq API.");
        }

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", AppConfig.get("groq.model", "llama-3.3-70b-versatile").trim());
            requestBody.addProperty("temperature", 0.1);
            requestBody.addProperty("max_tokens", AppConfig.getInt("groq.max.output.tokens", 900));

            JsonObject responseFormat = new JsonObject();
            responseFormat.addProperty("type", "json_object");
            requestBody.add("response_format", responseFormat);

            JsonArray messages = new JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", "Bạn là chuyên gia phân tích code thi đấu lập trình. Chỉ trả về một JSON hợp lệ, không thêm markdown.");
            messages.add(system);

            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", userPrompt);
            messages.add(user);
            requestBody.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.get("groq.api.url")))
                    .timeout(Duration.ofSeconds(AppConfig.getInt("groq.timeout.seconds", 60)))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            waitForRateLimitSlot();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(buildErrorMessage(response.statusCode(), response.body()));
            }

            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException("Groq API không trả về nội dung phân tích.");
            }
            return choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Yêu cầu Groq API bị gián đoạn.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Không gọi được Groq API: " + e.getMessage(), e);
        }
    }

    public boolean testConnection() {
        if (!AppConfig.getBoolean("groq.test.real.call", false)) {
            String apiKey = AppConfig.get("groq.api.key").trim();
            String apiUrl = AppConfig.get("groq.api.url").trim();
            String model = AppConfig.get("groq.model").trim();
            if (apiKey.isBlank()) {
                throw new IllegalStateException("Chưa cấu hình khóa Groq API.");
            }
            if (!apiKey.startsWith("gsk_")) {
                throw new IllegalStateException("Khóa Groq API không đúng định dạng. Khóa thường bắt đầu bằng gsk_.");
            }
            if (apiUrl.isBlank() || model.isBlank()) {
                throw new IllegalStateException("Chưa cấu hình đầy đủ đường dẫn Groq API hoặc tên model.");
            }
            return true;
        }
        String prompt = """
                Hãy trả về JSON đúng mẫu sau:
                {"trang_thai":"thành công","ghi_chu":"kiểm tra kết nối"}
                """;
        String response = chatJson(prompt);
        return response != null && response.contains("thành công");
    }

    private void waitForRateLimitSlot() throws InterruptedException {
        int intervalSeconds = AppConfig.getInt("groq.min.request.interval.seconds", 15);
        if (intervalSeconds <= 0) {
            return;
        }
        synchronized (REQUEST_LOCK) {
            long now = System.currentTimeMillis();
            long waitMillis = intervalSeconds * 1000L - (now - lastRequestTimeMillis);
            if (waitMillis > 0) {
                Thread.sleep(waitMillis);
            }
            lastRequestTimeMillis = System.currentTimeMillis();
        }
    }

    private String buildErrorMessage(int statusCode, String responseBody) {
        String serverMessage = extractServerErrorMessage(responseBody);
        return switch (statusCode) {
            case 400 -> "Groq API báo yêu cầu không hợp lệ. Hãy kiểm tra tên model và nội dung gửi đi. Chi tiết: " + serverMessage;
            case 401 -> "Groq API từ chối khóa API. Khóa có thể sai, đã bị thu hồi, chưa lưu đúng hoặc app chưa được khởi động lại sau khi đổi khóa.";
            case 403 -> "Groq API không cho phép dùng tài nguyên này. Hãy kiểm tra quyền của khóa API, tổ chức hoặc quyền dùng model đã cấu hình.";
            case 429 -> "Groq API báo vượt giới hạn sử dụng hoặc hết hạn mức. Hãy chờ một lúc, giảm số lần phân tích hoặc kiểm tra hạn mức tài khoản Groq.";
            default -> "Groq API trả về lỗi mã " + statusCode + ". Chi tiết: " + serverMessage;
        };
    }

    private String extractServerErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "Không có nội dung lỗi từ máy chủ.";
        }
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            if (root.has("error") && root.get("error").isJsonObject()) {
                JsonObject error = root.getAsJsonObject("error");
                if (error.has("message")) {
                    return error.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {
            // Nếu máy chủ không trả JSON, dùng chuỗi rút gọn bên dưới.
        }
        return responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody;
    }
}
