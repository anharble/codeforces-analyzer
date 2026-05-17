package com.codeforces.analyzer.model;

import java.time.LocalDateTime;

public class AnalysisResult {
    private long id;
    private long submissionId;
    private String detectedAlgorithms;
    private String detectedDataStructures;
    private String difficultyLevel;
    private int aiProbability;
    private String aiConfidence;
    private String aiReasons;
    private String aiComment;
    private int dataStructureScore;
    private int algorithmScore;
    private LocalDateTime analysisTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(long submissionId) {
        this.submissionId = submissionId;
    }

    public String getDetectedAlgorithms() {
        return detectedAlgorithms;
    }

    public void setDetectedAlgorithms(String detectedAlgorithms) {
        this.detectedAlgorithms = detectedAlgorithms;
    }

    public String getDetectedDataStructures() {
        return detectedDataStructures;
    }

    public void setDetectedDataStructures(String detectedDataStructures) {
        this.detectedDataStructures = detectedDataStructures;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getAiProbability() {
        return aiProbability;
    }

    public void setAiProbability(int aiProbability) {
        this.aiProbability = aiProbability;
    }

    public String getAiConfidence() {
        return aiConfidence;
    }

    public void setAiConfidence(String aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public String getAiReasons() {
        return aiReasons;
    }

    public void setAiReasons(String aiReasons) {
        this.aiReasons = aiReasons;
    }

    public String getAiComment() {
        return aiComment;
    }

    public void setAiComment(String aiComment) {
        this.aiComment = aiComment;
    }

    public int getDataStructureScore() {
        return dataStructureScore;
    }

    public void setDataStructureScore(int dataStructureScore) {
        this.dataStructureScore = dataStructureScore;
    }

    public int getAlgorithmScore() {
        return algorithmScore;
    }

    public void setAlgorithmScore(int algorithmScore) {
        this.algorithmScore = algorithmScore;
    }

    public LocalDateTime getAnalysisTime() {
        return analysisTime;
    }

    public void setAnalysisTime(LocalDateTime analysisTime) {
        this.analysisTime = analysisTime;
    }
}
