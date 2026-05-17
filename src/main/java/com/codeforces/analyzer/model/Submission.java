package com.codeforces.analyzer.model;

import java.time.LocalDateTime;

public class Submission {
    private long id;
    private long userId;
    private String submissionId;
    private String problemName;
    private String verdict;
    private String language;
    private LocalDateTime submitTime;
    private String sourceCode;
    private String sourceUrl;
    private String sourceCrawlStatus;
    private String sourceCrawlMessage;
    private LocalDateTime crawlTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getProblemName() {
        return problemName;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getSourceCrawlStatus() {
        return sourceCrawlStatus;
    }

    public void setSourceCrawlStatus(String sourceCrawlStatus) {
        this.sourceCrawlStatus = sourceCrawlStatus;
    }

    public String getSourceCrawlMessage() {
        return sourceCrawlMessage;
    }

    public void setSourceCrawlMessage(String sourceCrawlMessage) {
        this.sourceCrawlMessage = sourceCrawlMessage;
    }

    public LocalDateTime getCrawlTime() {
        return crawlTime;
    }

    public void setCrawlTime(LocalDateTime crawlTime) {
        this.crawlTime = crawlTime;
    }
}
