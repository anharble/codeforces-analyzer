package com.codeforces.analyzer.model;

import java.time.LocalDateTime;

public class CrawlLog {
    private long id;
    private Long userId;
    private String crawlStatus;
    private String crawlMessage;
    private LocalDateTime crawlTime;

    public CrawlLog() {
    }

    public CrawlLog(Long userId, String crawlStatus, String crawlMessage) {
        this.userId = userId;
        this.crawlStatus = crawlStatus;
        this.crawlMessage = crawlMessage;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCrawlStatus() {
        return crawlStatus;
    }

    public void setCrawlStatus(String crawlStatus) {
        this.crawlStatus = crawlStatus;
    }

    public String getCrawlMessage() {
        return crawlMessage;
    }

    public void setCrawlMessage(String crawlMessage) {
        this.crawlMessage = crawlMessage;
    }

    public LocalDateTime getCrawlTime() {
        return crawlTime;
    }

    public void setCrawlTime(LocalDateTime crawlTime) {
        this.crawlTime = crawlTime;
    }
}
