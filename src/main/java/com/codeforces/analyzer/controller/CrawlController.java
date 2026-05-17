package com.codeforces.analyzer.controller;

import com.codeforces.analyzer.crawler.SeleniumCrawler;
import com.codeforces.analyzer.model.User;
import com.codeforces.analyzer.scheduler.CrawlScheduler;

public class CrawlController {
    private final SeleniumCrawler seleniumCrawler;
    private final CrawlScheduler crawlScheduler;

    public CrawlController(SeleniumCrawler seleniumCrawler, CrawlScheduler crawlScheduler) {
        this.seleniumCrawler = seleniumCrawler;
        this.crawlScheduler = crawlScheduler;
    }

    public int crawlUser(User user) {
        return seleniumCrawler.crawlUser(user);
    }

    public int crawlAllNow() {
        return crawlScheduler.crawlAllUsersNow();
    }

    public void startScheduler() {
        crawlScheduler.start();
    }

    public void stopScheduler() {
        crawlScheduler.stop();
    }

    public boolean isSchedulerRunning() {
        return crawlScheduler.isRunning();
    }
}
