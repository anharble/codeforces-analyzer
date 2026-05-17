package com.codeforces.analyzer.model;

public class UserEvaluation {
    private int dataStructureScore;
    private int algorithmScore;
    private int problemSolvingScore;
    private int aiUsageScore;
    private double acceptedRate;
    private String commonLanguage;
    private String rankName;
    private String summary;

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

    public int getProblemSolvingScore() {
        return problemSolvingScore;
    }

    public void setProblemSolvingScore(int problemSolvingScore) {
        this.problemSolvingScore = problemSolvingScore;
    }

    public int getAiUsageScore() {
        return aiUsageScore;
    }

    public void setAiUsageScore(int aiUsageScore) {
        this.aiUsageScore = aiUsageScore;
    }

    public double getAcceptedRate() {
        return acceptedRate;
    }

    public void setAcceptedRate(double acceptedRate) {
        this.acceptedRate = acceptedRate;
    }

    public String getCommonLanguage() {
        return commonLanguage;
    }

    public void setCommonLanguage(String commonLanguage) {
        this.commonLanguage = commonLanguage;
    }

    public String getRankName() {
        return rankName;
    }

    public void setRankName(String rankName) {
        this.rankName = rankName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
