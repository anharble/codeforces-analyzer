package com.codeforces.analyzer.controller;

import java.util.List;

import com.codeforces.analyzer.model.AnalysisResult;
import com.codeforces.analyzer.service.AnalysisService;

public class AnalysisController {
    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    public AnalysisResult analyzeSubmission(long submissionId) {
        return analysisService.analyzeSubmission(submissionId);
    }

    public List<AnalysisResult> analyzeAllPending() {
        return analysisService.analyzeAllPending();
    }

    public AnalysisResult findBySubmissionId(long submissionId) {
        return analysisService.findBySubmissionId(submissionId);
    }

    public List<AnalysisResult> findAll() {
        return analysisService.findAll();
    }
}
