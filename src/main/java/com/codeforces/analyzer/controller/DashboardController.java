package com.codeforces.analyzer.controller;

import com.codeforces.analyzer.analyzer.GroqService;
import com.codeforces.analyzer.database.DatabaseConnection;
import com.codeforces.analyzer.model.DashboardStats;
import com.codeforces.analyzer.model.UserEvaluation;
import com.codeforces.analyzer.service.StatisticsService;

public class DashboardController {
    private final StatisticsService statisticsService;
    private final GroqService groqService;

    public DashboardController(StatisticsService statisticsService, GroqService groqService) {
        this.statisticsService = statisticsService;
        this.groqService = groqService;
    }

    public DashboardStats getDashboardStats() {
        return statisticsService.getDashboardStats();
    }

    public UserEvaluation evaluateUser(long userId) {
        return statisticsService.evaluateUser(userId);
    }

    public boolean testDatabase() {
        return DatabaseConnection.testConnection();
    }

    public boolean testGroqApi() {
        return groqService.testConnection();
    }
}
