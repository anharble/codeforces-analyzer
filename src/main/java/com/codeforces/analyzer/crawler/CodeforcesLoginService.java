package com.codeforces.analyzer.crawler;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeforces.analyzer.model.CfAccount;
import com.codeforces.analyzer.service.AccountService;
import com.codeforces.analyzer.utils.AppConfig;

public class CodeforcesLoginService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeforcesLoginService.class);

    private final SessionManager sessionManager;
    private final CookieManager cookieManager;
    private final AccountService accountService;

    public CodeforcesLoginService(SessionManager sessionManager, CookieManager cookieManager, AccountService accountService) {
        this.sessionManager = sessionManager;
        this.cookieManager = cookieManager;
        this.accountService = accountService;
    }

    public boolean dangNhap(CfAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Chưa có tài khoản Codeforces để đăng nhập.");
        }
        WebDriver driver = sessionManager.getDriver();
        try {
            cookieManager.loadCookies(account, driver);
            if (isLoggedIn(driver)) {
                cookieManager.saveCookies(account, driver);
                LOGGER.info("Đã dùng lại phiên đăng nhập Codeforces của {}.", account.getUsername());
                return true;
            }

            driver.manage().deleteAllCookies();
            driver.get(AppConfig.get("codeforces.login.url", "https://codeforces.com/enter"));
            int timeout = AppConfig.getInt("selenium.timeout.seconds", 30);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            waitForPageReady(driver, timeout);

            if (hasManualChallenge(driver)) {
                LOGGER.warn("Codeforces đang yêu cầu xác minh trình duyệt. Vui lòng xử lý trong cửa sổ Chrome đang mở.");
                waitForLoginFormOrLoggedIn(driver, AppConfig.getInt("selenium.manual.captcha.seconds", 180));
                if (!isLoggedIn(driver) && !hasLoginForm(driver)) {
                    throw new IllegalStateException(
                            "Codeforces vẫn đang ở trang xác minh. URL đăng nhập đã đúng là https://codeforces.com/enter, "
                                    + "nhưng bạn cần hoàn tất trang \"Just a moment\" trong cửa sổ Chrome Selenium rồi bấm Đăng nhập lại.");
                }
            }

            if (isLoggedIn(driver)) {
                cookieManager.saveCookies(account, driver);
                LOGGER.info("Đăng nhập Codeforces thành công với tài khoản {}.", account.getUsername());
                return true;
            }

            WebElement usernameInput = findLoginInput(driver, wait);
            WebElement passwordInput = findPasswordInput(driver, wait);

            usernameInput.clear();
            usernameInput.sendKeys(account.getUsername());
            passwordInput.clear();
            passwordInput.sendKeys(accountService.decryptPassword(account));
            findSubmitButton(driver, wait).click();

            if (hasManualChallenge(driver)) {
                LOGGER.warn("Codeforces yêu cầu mã xác minh. Vui lòng giải mã xác minh trong cửa sổ Chrome đang mở.");
                waitForManualCaptcha(driver);
            }

            boolean success = new WebDriverWait(driver, Duration.ofSeconds(timeout))
                    .until(currentDriver -> isLoggedIn(currentDriver));
            if (success) {
                cookieManager.saveCookies(account, driver);
                LOGGER.info("Đăng nhập Codeforces thành công với tài khoản {}.", account.getUsername());
                return true;
            }
            accountService.updateStatus(account.getId(), "Đăng nhập thất bại");
            return false;
        } catch (Exception e) {
            accountService.updateStatus(account.getId(), "Đăng nhập thất bại");
            if (e instanceof IllegalStateException) {
                throw (IllegalStateException) e;
            }
            throw new IllegalStateException("Đăng nhập Codeforces thất bại: " + e.getMessage(), e);
        }
    }

    public boolean kiemTraDangNhap(CfAccount account) {
        if (account == null) {
            return false;
        }
        WebDriver driver = sessionManager.getDriver();
        try {
            cookieManager.loadCookies(account, driver);
            driver.get(AppConfig.get("codeforces.base.url", "https://codeforces.com"));
            boolean loggedIn = isLoggedIn(driver);
            accountService.updateStatus(account.getId(), loggedIn ? "Đã đăng nhập" : "Đã hết phiên");
            return loggedIn;
        } catch (Exception e) {
            accountService.updateStatus(account.getId(), "Kiểm tra thất bại");
            LOGGER.warn("Không kiểm tra được trạng thái đăng nhập: {}", e.getMessage());
            return false;
        }
    }

    public void dangXuat(CfAccount account) {
        if (account == null) {
            throw new IllegalArgumentException("Vui lòng chọn tài khoản cần đăng xuất.");
        }
        WebDriver driver = sessionManager.getDriver();
        driver.manage().deleteAllCookies();
        accountService.updateSession(account.getId(), "", "Đã đăng xuất");
        LOGGER.info("Đã xóa dữ liệu phiên đăng nhập của tài khoản {}.", account.getUsername());
    }

    public boolean isLoggedIn(WebDriver driver) {
        try {
            if (!driver.findElements(By.cssSelector("a[href*='logout']")).isEmpty()) {
                return true;
            }
            String source = driver.getPageSource();
            return source != null && source.contains("/logout");
        } catch (Exception e) {
            return false;
        }
    }

    private WebElement findLoginInput(WebDriver driver, WebDriverWait wait) {
        return findFirstVisible(driver, wait,
                "ô nhập tài khoản Codeforces",
                loginInputLocators());
    }

    private WebElement findPasswordInput(WebDriver driver, WebDriverWait wait) {
        return findFirstVisible(driver, wait,
                "ô nhập mật khẩu Codeforces",
                By.name("password"),
                By.cssSelector("input[name='password']"),
                By.cssSelector("input[type='password']"));
    }

    private WebElement findSubmitButton(WebDriver driver, WebDriverWait wait) {
        return findFirstVisible(driver, wait,
                "nút đăng nhập Codeforces",
                By.cssSelector("input[type='submit']"),
                By.cssSelector("button[type='submit']"),
                By.cssSelector(".submit"),
                By.cssSelector("input[value*='Login']"),
                By.cssSelector("input[value*='Đăng nhập']"));
    }

    private WebElement findFirstVisible(WebDriver driver, WebDriverWait wait, String elementName, By... locators) {
        try {
            return wait.until(currentDriver -> {
                for (By locator : locators) {
                    List<WebElement> elements = currentDriver.findElements(locator);
                    for (WebElement element : elements) {
                        if (element.isDisplayed() && element.isEnabled()) {
                            return element;
                        }
                    }
                }
                return null;
            });
        } catch (TimeoutException e) {
            throw new IllegalStateException(
                    "Không tìm thấy " + elementName + ". Trang hiện tại: " + safeCurrentUrl(driver)
                            + ". Tiêu đề trang: " + safeTitle(driver)
                            + ". Hãy kiểm tra cửa sổ Chrome Selenium: Codeforces có thể đang yêu cầu xác minh, bị lỗi mạng hoặc đã đổi giao diện đăng nhập.",
                    e);
        }
    }

    private void waitForPageReady(WebDriver driver, int timeout) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(currentDriver -> {
                Object state = ((JavascriptExecutor) currentDriver).executeScript("return document.readyState");
                return "complete".equals(state) || "interactive".equals(state);
            });
        } catch (Exception e) {
            LOGGER.warn("Trang đăng nhập chưa báo tải xong, tiếp tục dò form đăng nhập: {}", e.getMessage());
        }
    }

    private void waitForLoginFormOrLoggedIn(WebDriver driver, int timeout) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(currentDriver ->
                    isLoggedIn(currentDriver) || hasLoginForm(currentDriver));
        } catch (TimeoutException e) {
            LOGGER.warn("Hết thời gian chờ xác minh trình duyệt Codeforces.");
        }
    }

    private boolean hasLoginForm(WebDriver driver) {
        return hasVisibleElement(driver, By.cssSelector("input[type='password']"))
                && hasAnyVisibleElement(driver, loginInputLocators());
    }

    private boolean hasAnyVisibleElement(WebDriver driver, By... locators) {
        for (By locator : locators) {
            if (hasVisibleElement(driver, locator)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasVisibleElement(WebDriver driver, By locator) {
        try {
            for (WebElement element : driver.findElements(locator)) {
                if (element.isDisplayed()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasManualChallenge(WebDriver driver) {
        String source = driver.getPageSource();
        String lower = source == null ? "" : source.toLowerCase();
        String title = safeTitle(driver).toLowerCase();
        return lower.contains("captcha")
                || lower.contains("checking your browser")
                || lower.contains("verify you are human")
                || lower.contains("unusual traffic")
                || lower.contains("just a moment")
                || title.contains("just a moment");
    }

    private void waitForManualCaptcha(WebDriver driver) {
        int seconds = AppConfig.getInt("selenium.manual.captcha.seconds", 180);
        new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(currentDriver -> isLoggedIn(currentDriver));
    }

    private String safeCurrentUrl(WebDriver driver) {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            return "không đọc được";
        }
    }

    private String safeTitle(WebDriver driver) {
        try {
            return driver.getTitle();
        } catch (Exception e) {
            return "không đọc được";
        }
    }

    private By[] loginInputLocators() {
        return new By[]{
                By.name("handleOrEmail"),
                By.cssSelector("input[name='handleOrEmail']"),
                By.cssSelector("input#handleOrEmail"),
                By.cssSelector("input[name='handle']"),
                By.cssSelector("input[name='email']"),
                By.cssSelector("input[type='email']"),
                By.cssSelector("input[type='text']")
        };
    }
}
