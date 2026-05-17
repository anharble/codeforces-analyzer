package com.codeforces.analyzer.model;

public enum Platform {
    CODEFORCES("Codeforces"),
    VJUDGE("VJudge");

    private final String tenHienThi;

    Platform(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    @Override
    public String toString() {
        return tenHienThi;
    }
}
