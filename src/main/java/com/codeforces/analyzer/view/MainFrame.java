package com.codeforces.analyzer.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.codeforces.analyzer.analyzer.GroqAnalyzer;
import com.codeforces.analyzer.analyzer.GroqService;
import com.codeforces.analyzer.analyzer.HeuristicAnalyzer;
import com.codeforces.analyzer.controller.AccountController;
import com.codeforces.analyzer.controller.AnalysisController;
import com.codeforces.analyzer.controller.CrawlController;
import com.codeforces.analyzer.controller.DashboardController;
import com.codeforces.analyzer.controller.UserController;
import com.codeforces.analyzer.crawler.CodeforcesLoginService;
import com.codeforces.analyzer.crawler.CookieManager;
import com.codeforces.analyzer.crawler.SeleniumCrawler;
import com.codeforces.analyzer.crawler.SessionManager;
import com.codeforces.analyzer.dao.AnalysisDAO;
import com.codeforces.analyzer.dao.CfAccountDAO;
import com.codeforces.analyzer.dao.CrawlLogDAO;
import com.codeforces.analyzer.dao.SubmissionDAO;
import com.codeforces.analyzer.dao.UserDAO;
import com.codeforces.analyzer.scheduler.CrawlScheduler;
import com.codeforces.analyzer.service.AccountService;
import com.codeforces.analyzer.service.AnalysisService;
import com.codeforces.analyzer.service.StatisticsService;
import com.codeforces.analyzer.service.SubmissionService;
import com.codeforces.analyzer.service.UserService;

public class MainFrame extends JFrame {
    private final SessionManager sessionManager;
    private final CrawlScheduler crawlScheduler;

    public MainFrame() {
        super("Bộ phân tích Codeforces");

        UserDAO userDAO = new UserDAO();
        SubmissionDAO submissionDAO = new SubmissionDAO();
        AnalysisDAO analysisDAO = new AnalysisDAO();
        CrawlLogDAO crawlLogDAO = new CrawlLogDAO();
        CfAccountDAO accountDAO = new CfAccountDAO();

        UserService userService = new UserService(userDAO);
        AccountService accountService = new AccountService(accountDAO);
        SubmissionService submissionService = new SubmissionService(submissionDAO);
        StatisticsService statisticsService = new StatisticsService(userDAO, submissionDAO, analysisDAO);

        GroqService groqService = new GroqService();
        GroqAnalyzer groqAnalyzer = new GroqAnalyzer(groqService, new HeuristicAnalyzer());
        AnalysisService analysisService = new AnalysisService(analysisDAO, submissionDAO, groqAnalyzer);

        sessionManager = new SessionManager();
        CookieManager cookieManager = new CookieManager(accountService);
        CodeforcesLoginService loginService = new CodeforcesLoginService(sessionManager, cookieManager, accountService);
        SeleniumCrawler seleniumCrawler = new SeleniumCrawler(sessionManager, loginService, accountService, submissionDAO, crawlLogDAO);
        crawlScheduler = new CrawlScheduler(userService, seleniumCrawler);

        UserController userController = new UserController(userService);
        AccountController accountController = new AccountController(accountService, loginService);
        CrawlController crawlController = new CrawlController(seleniumCrawler, crawlScheduler);
        AnalysisController analysisController = new AnalysisController(analysisService);
        DashboardController dashboardController = new DashboardController(statisticsService, groqService);

        buildUi(userController, accountController, crawlController, analysisController,
                dashboardController, submissionService, statisticsService);
        registerShutdown();
    }

    private void buildUi(UserController userController,
                         AccountController accountController,
                         CrawlController crawlController,
                         AnalysisController analysisController,
                         DashboardController dashboardController,
                         SubmissionService submissionService,
                         StatisticsService statisticsService) {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Tổng quan", new DashboardPanel(dashboardController, crawlController));
        tabs.addTab("Quản lý nick", new UserManagementPanel(userController));
        tabs.addTab("Xem bài nộp", new SubmissionViewerPanel(userController, submissionService, crawlController, analysisController));
        tabs.addTab("Phân tích AI", new AIAnalysisPanel(userController, submissionService, analysisController, statisticsService));
        tabs.addTab("Thống kê", new StatisticsPanel(userController, dashboardController));
        tabs.addTab("Tài khoản Codeforces", new AccountManagerPanel(accountController));
        tabs.addTab("Trạng thái đăng nhập", new LoginStatusPanel(accountController));

        tabs.addChangeListener(e -> {
            Object selected = tabs.getSelectedComponent();
            if (selected instanceof Refreshable refreshable) {
                refreshable.refreshData();
            }
        });

        add(tabs, BorderLayout.CENTER);
    }

    private void registerShutdown() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                crawlScheduler.stop();
                sessionManager.close();
                dispose();
                System.exit(0);
            }
        });
    }
}
