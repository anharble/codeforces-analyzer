package com.codeforces.analyzer.model;

import java.time.LocalDateTime;

public class User {
    private long id;
    private String handle;
    private Platform platform;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(long id, String handle, Platform platform, LocalDateTime createdAt) {
        this.id = id;
        this.handle = handle;
        this.platform = platform;
        this.createdAt = createdAt;
    }

    public User(String handle, Platform platform) {
        this.handle = handle;
        this.platform = platform;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return handle + " - " + platform.getTenHienThi();
    }
}
