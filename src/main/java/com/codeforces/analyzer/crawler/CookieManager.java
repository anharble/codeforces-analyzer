package com.codeforces.analyzer.crawler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeforces.analyzer.model.CfAccount;
import com.codeforces.analyzer.service.AccountService;
import com.codeforces.analyzer.utils.AppConfig;
import com.codeforces.analyzer.utils.JsonUtils;
import com.google.gson.reflect.TypeToken;

public class CookieManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CookieManager.class);

    private final AccountService accountService;

    public CookieManager(AccountService accountService) {
        this.accountService = accountService;
    }

    public void saveCookies(CfAccount account, WebDriver driver) {
        List<CookieData> cookies = new ArrayList<>();
        for (Cookie cookie : driver.manage().getCookies()) {
            CookieData data = new CookieData();
            data.name = cookie.getName();
            data.value = cookie.getValue();
            data.domain = cookie.getDomain();
            data.path = cookie.getPath();
            data.expiry = cookie.getExpiry() == null ? null : cookie.getExpiry().getTime();
            data.secure = cookie.isSecure();
            data.httpOnly = cookie.isHttpOnly();
            cookies.add(data);
        }
        accountService.updateSession(account.getId(), JsonUtils.gson().toJson(cookies), "Đã đăng nhập");
        LOGGER.info("Đã lưu dữ liệu phiên đăng nhập cho tài khoản {}.", account.getUsername());
    }

    public void loadCookies(CfAccount account, WebDriver driver) {
        if (account == null || account.getCookies() == null || account.getCookies().isBlank()) {
            return;
        }
        driver.get(AppConfig.get("codeforces.base.url", "https://codeforces.com"));
        List<CookieData> cookies = JsonUtils.gson().fromJson(account.getCookies(), new TypeToken<List<CookieData>>() {
        }.getType());
        if (cookies == null) {
            return;
        }
        for (CookieData data : cookies) {
            try {
                Cookie.Builder builder = new Cookie.Builder(data.name, data.value)
                        .path(data.path == null || data.path.isBlank() ? "/" : data.path)
                        .isSecure(Boolean.TRUE.equals(data.secure))
                        .isHttpOnly(Boolean.TRUE.equals(data.httpOnly));
                if (data.domain != null && !data.domain.isBlank()) {
                    builder.domain(data.domain);
                }
                if (data.expiry != null) {
                    builder.expiresOn(new Date(data.expiry));
                }
                driver.manage().addCookie(builder.build());
            } catch (Exception e) {
                LOGGER.warn("Bỏ qua một dữ liệu phiên không hợp lệ: {}", e.getMessage());
            }
        }
        driver.navigate().refresh();
        LOGGER.info("Đã nạp dữ liệu phiên đăng nhập cho tài khoản {}.", account.getUsername());
    }

    private static class CookieData {
        String name;
        String value;
        String domain;
        String path;
        Long expiry;
        Boolean secure;
        Boolean httpOnly;
    }
}
