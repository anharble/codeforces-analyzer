package com.codeforces.analyzer.scheduler;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeforces.analyzer.crawler.SeleniumCrawler;
import com.codeforces.analyzer.model.User;
import com.codeforces.analyzer.service.UserService;
import com.codeforces.analyzer.utils.AppConfig;

public class CrawlScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlScheduler.class);

    private final UserService userService;
    private final SeleniumCrawler seleniumCrawler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledExecutorService executorService;

    public CrawlScheduler(UserService userService, SeleniumCrawler seleniumCrawler) {
        this.userService = userService;
        this.seleniumCrawler = seleniumCrawler;
    }

    public synchronized void start() {
        if (running.get()) {
            LOGGER.info("Lịch thu thập tự động đang chạy.");
            return;
        }
        int intervalHours = AppConfig.getInt("scheduler.interval.hours", 24);
        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "lich-crawl-tu-dong");
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleAtFixedRate(this::crawlAllUsersSafely, 0, intervalHours, TimeUnit.HOURS);
        running.set(true);
        LOGGER.info("Đã bật thu thập tự động mỗi {} giờ.", intervalHours);
    }

    public synchronized void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        running.set(false);
        LOGGER.info("Đã tắt thu thập tự động.");
    }

    public boolean isRunning() {
        return running.get();
    }

    public int crawlAllUsersNow() {
        return crawlAllUsersSafely();
    }

    private int crawlAllUsersSafely() {
        int totalNew = 0;
        try {
            List<User> users = userService.getAllUsers();
            for (User user : users) {
                try {
                    totalNew += seleniumCrawler.crawlUser(user);
                } catch (Exception e) {
                    LOGGER.warn("Thu thập tự động thất bại với {}: {}", user.getHandle(), e.getMessage());
                }
            }
            LOGGER.info("Thu thập tự động hoàn tất. Bài nộp mới: {}.", totalNew);
            return totalNew;
        } catch (Exception e) {
            LOGGER.error("Thu thập tự động bị lỗi: {}", e.getMessage());
            return totalNew;
        }
    }
}
