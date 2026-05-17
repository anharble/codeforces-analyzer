package com.codeforces.analyzer.crawler;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeforces.analyzer.utils.AppConfig;

public class SessionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    private WebDriver driver;

    public synchronized WebDriver getDriver() {
        if (driver == null || !isAlive()) {
            driver = createDriver();
        }
        return driver;
    }

    public synchronized void close() {
        if (driver != null) {
            try {
                driver.quit();
                LOGGER.info("Đã đóng trình duyệt Selenium.");
            } catch (Exception e) {
                LOGGER.warn("Không đóng được trình duyệt Selenium: {}", e.getMessage());
            } finally {
                driver = null;
            }
        }
    }

    private WebDriver createDriver() {
        String driverPath = AppConfig.get("selenium.chromedriver.path");
        if (driverPath != null && !driverPath.isBlank()) {
            System.setProperty("webdriver.chrome.driver", Path.of(driverPath).toAbsolutePath().toString());
        }

        ChromeOptions options = new ChromeOptions();
        String binary = AppConfig.get("selenium.chrome.binary");
        if (binary != null && !binary.isBlank()) {
            options.setBinary(Path.of(binary).toAbsolutePath().toString());
        }
        if (AppConfig.getBoolean("selenium.headless", false)) {
            options.addArguments("--headless=new");
        }
        String profileDir = AppConfig.get("selenium.user.data.dir", "selenium-profile");
        options.addArguments("--user-data-dir=" + Path.of(profileDir).toAbsolutePath());
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        LOGGER.info("Đang mở Chrome bằng Selenium. Chrome mong muốn: {}, ChromeDriver mong muốn: {}.",
                AppConfig.get("selenium.expected.chrome.version"),
                AppConfig.get("selenium.expected.chromedriver.version"));

        WebDriver newDriver = new ChromeDriver(options);
        int timeout = AppConfig.getInt("selenium.timeout.seconds", 30);
        newDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        newDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
        return newDriver;
    }

    private boolean isAlive() {
        try {
            driver.getTitle();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
