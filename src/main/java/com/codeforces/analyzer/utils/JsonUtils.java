package com.codeforces.analyzer.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonUtils {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private JsonUtils() {
    }

    public static Gson gson() {
        return GSON;
    }

    public static String extractJsonObject(String text) {
        if (text == null) {
            return "{}";
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{}";
    }
}
