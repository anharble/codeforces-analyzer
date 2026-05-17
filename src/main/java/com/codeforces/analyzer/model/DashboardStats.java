package com.codeforces.analyzer.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardStats {
    private long totalUsers;
    private long totalSubmissions;
    private double aiUsageRate;
    private Map<String, Integer> topAlgorithms = new LinkedHashMap<>();
    private Map<String, Integer> topDataStructures = new LinkedHashMap<>();
    private Map<String, Integer> topUsers = new LinkedHashMap<>();

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public double getAiUsageRate() {
        return aiUsageRate;
    }

    public void setAiUsageRate(double aiUsageRate) {
        this.aiUsageRate = aiUsageRate;
    }

    public Map<String, Integer> getTopAlgorithms() {
        return topAlgorithms;
    }

    public void setTopAlgorithms(Map<String, Integer> topAlgorithms) {
        this.topAlgorithms = topAlgorithms;
    }

    public Map<String, Integer> getTopDataStructures() {
        return topDataStructures;
    }

    public void setTopDataStructures(Map<String, Integer> topDataStructures) {
        this.topDataStructures = topDataStructures;
    }

    public Map<String, Integer> getTopUsers() {
        return topUsers;
    }

    public void setTopUsers(Map<String, Integer> topUsers) {
        this.topUsers = topUsers;
    }
}
