package com.codeforces.analyzer.crawler;

import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeforces.analyzer.dao.CrawlLogDAO;
import com.codeforces.analyzer.dao.SubmissionDAO;
import com.codeforces.analyzer.model.CfAccount;
import com.codeforces.analyzer.model.CrawlLog;
import com.codeforces.analyzer.model.Platform;
import com.codeforces.analyzer.model.Submission;
import com.codeforces.analyzer.model.User;
import com.codeforces.analyzer.service.AccountService;
import com.codeforces.analyzer.utils.AppConfig;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class SeleniumCrawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumCrawler.class);
    private static final Pattern SUBMISSION_ID_PATTERN = Pattern.compile("(\\d+)");

    private final SessionManager sessionManager;
    private final CodeforcesLoginService loginService;
    private final AccountService accountService;
    private final SubmissionDAO submissionDAO;
    private final CrawlLogDAO crawlLogDAO;

    public SeleniumCrawler(SessionManager sessionManager,
                           CodeforcesLoginService loginService,
                           AccountService accountService,
                           SubmissionDAO submissionDAO,
                           CrawlLogDAO crawlLogDAO) {
        this.sessionManager = sessionManager;
        this.loginService = loginService;
        this.accountService = accountService;
        this.submissionDAO = submissionDAO;
        this.crawlLogDAO = crawlLogDAO;
    }

    public int crawlUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Vui lòng chọn nick cần thu thập.");
        }
        if (user.getPlatform() == Platform.VJUDGE) {
            return crawlVJudge(user);
        }
        return crawlCodeforces(user);
    }

    private int crawlCodeforces(User user) {
        int newCount = 0;
        try {
            CfAccount account = accountService.findFirstAvailable();
            if (account != null) {
                loginService.dangNhap(account);
            } else {
                LOGGER.warn("Chưa có tài khoản Codeforces. Hệ thống vẫn thu thập danh sách bài nộp, nhưng có thể không xem được mã nguồn.");
            }

            WebDriver driver = sessionManager.getDriver();
            int maxPages = AppConfig.getInt("crawler.max.pages", 3);
            int maxSubmissions = AppConfig.getInt("crawler.max.submissions", 10);
            int processedCount = 0;
            for (int page = 1; page <= maxPages; page++) {
                List<Submission> pageSubmissions = readSubmissionPage(driver, user, page);
                if (pageSubmissions.isEmpty()) {
                    break;
                }
                for (Submission draft : pageSubmissions) {
                    if (processedCount >= maxSubmissions) {
                        break;
                    }
                    Submission current = submissionDAO.findByUserAndSubmissionId(user.getId(), draft.getSubmissionId());
                    if (current == null) {
                        submissionDAO.saveIfNew(draft);
                        current = draft;
                        newCount++;
                    } else {
                        draft.setId(current.getId());
                        submissionDAO.updateMetadata(draft);
                        current = submissionDAO.findById(current.getId());
                    }
                    crawlSourceIfNeeded(current, account);
                    processedCount++;
                }
                if (processedCount >= maxSubmissions) {
                    break;
                }
            }
            crawlLogDAO.save(new CrawlLog(user.getId(), "Thành công",
                    "Đã thu thập Codeforces cho " + user.getHandle()
                            + ", đã xử lý: " + processedCount
                            + ", bài nộp mới: " + newCount));
            return newCount;
        } catch (Exception e) {
            saveLog(user, "Thất bại", "Thu thập Codeforces thất bại: " + e.getMessage());
            throw new IllegalStateException("Thu thập Codeforces thất bại: " + e.getMessage(), e);
        }
    }

    private int crawlVJudge(User user) {
        try {
            WebDriver driver = sessionManager.getDriver();
            driver.get("https://vjudge.net/user/" + user.getHandle());
            new WebDriverWait(driver, Duration.ofSeconds(AppConfig.getInt("selenium.timeout.seconds", 30)))
                    .until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            crawlLogDAO.save(new CrawlLog(user.getId(), "Thành công",
                    "Đã mở trang VJudge của " + user.getHandle() + ". VJudge có thể giới hạn quyền xem mã nguồn công khai."));
            return 0;
        } catch (Exception e) {
            saveLog(user, "Thất bại", "Thu thập VJudge thất bại: " + e.getMessage());
            throw new IllegalStateException("Thu thập VJudge thất bại: " + e.getMessage(), e);
        }
    }

    private List<Submission> readSubmissionPage(WebDriver driver, User user, int page) {
        String baseUrl = AppConfig.get("codeforces.base.url", "https://codeforces.com");
        String url = page == 1
                ? baseUrl + "/submissions/" + user.getHandle()
                : baseUrl + "/submissions/" + user.getHandle() + "/page/" + page;
        driver.get(url);
        int timeout = AppConfig.getInt("selenium.timeout.seconds", 30);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeout))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.status-frame-datatable")));
        } catch (TimeoutException e) {
            LOGGER.warn("Không tìm thấy bảng bài nộp ở trang {} của {}.", page, user.getHandle());
            return List.of();
        }

        List<Submission> result = new ArrayList<>();
        List<WebElement> rows = driver.findElements(By.cssSelector("table.status-frame-datatable tr"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() < 6) {
                continue;
            }
            String submissionId = parseSubmissionId(cells.get(0).getText());
            if (submissionId == null) {
                continue;
            }
            Submission submission = new Submission();
            submission.setUserId(user.getId());
            submission.setSubmissionId(submissionId);
            submission.setSubmitTime(DateTimeUtils.parseCodeforcesTime(cells.get(1).getText()));
            submission.setProblemName(cleanCell(cells.get(3).getText()));
            submission.setLanguage(cleanCell(cells.get(4).getText()));
            submission.setVerdict(cleanCell(cells.get(5).getText()));
            submission.setSourceUrl(extractSourceUrl(cells.get(0)));
            submission.setSourceCrawlStatus("Đang chờ");
            submission.setSourceCrawlMessage("Đã tìm thấy bài nộp trong bảng Codeforces.");
            result.add(submission);
        }
        return result;
    }

    private void crawlSourceIfNeeded(Submission submission, CfAccount account) throws SQLException {
        boolean retryFailed = AppConfig.getBoolean("crawler.retry.failed.source", true);
        boolean hasSource = submission.getSourceCode() != null && !submission.getSourceCode().isBlank();
        boolean failedBefore = "Thất bại".equalsIgnoreCase(submission.getSourceCrawlStatus());
        if (hasSource || (!retryFailed && failedBefore)) {
            return;
        }
        if (submission.getSourceUrl() == null || submission.getSourceUrl().isBlank()) {
            submissionDAO.updateSource(submission.getId(), submission.getSourceCode(), "Thất bại", "Không tìm thấy đường dẫn mã nguồn.");
            return;
        }
        try {
            String source = crawlSourceCode(submission.getSourceUrl(), account);
            submissionDAO.updateSource(submission.getId(), source, "Thành công", "Đã thu thập mã nguồn thành công.");
        } catch (Exception e) {
            submissionDAO.updateSource(submission.getId(), submission.getSourceCode(), "Thất bại", e.getMessage());
            LOGGER.warn("Không thu thập được mã nguồn của bài nộp {}: {}", submission.getSubmissionId(), e.getMessage());
        }
    }

    private String crawlSourceCode(String sourceUrl, CfAccount account) {
        WebDriver driver = sessionManager.getDriver();
        if (account != null && !loginService.kiemTraDangNhap(account)) {
            loginService.dangNhap(account);
        }
        driver.get(sourceUrl);
        int timeout = AppConfig.getInt("selenium.timeout.seconds", 30);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        WebElement sourceElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#program-source-text, pre.prettyprint, pre")));
        String source = sourceElement.getText();
        if (source == null || source.isBlank()) {
            source = sourceElement.getAttribute("textContent");
        }
        if (source == null || source.isBlank()) {
            throw new IllegalStateException("Trang bài nộp không hiển thị mã nguồn.");
        }
        return source;
    }

    private String extractSourceUrl(WebElement idCell) {
        List<WebElement> links = idCell.findElements(By.tagName("a"));
        if (links.isEmpty()) {
            return null;
        }
        return links.get(0).getAttribute("href");
    }

    private String parseSubmissionId(String rawText) {
        if (rawText == null) {
            return null;
        }
        Matcher matcher = SUBMISSION_ID_PATTERN.matcher(rawText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String cleanCell(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    private void saveLog(User user, String status, String message) {
        try {
            crawlLogDAO.save(new CrawlLog(user == null ? null : user.getId(), status, message));
        } catch (SQLException e) {
            LOGGER.warn("Không lưu được nhật ký thu thập: {}", e.getMessage());
        }
    }
}
