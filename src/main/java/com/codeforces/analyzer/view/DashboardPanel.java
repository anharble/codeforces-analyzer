package com.codeforces.analyzer.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.codeforces.analyzer.controller.CrawlController;
import com.codeforces.analyzer.controller.DashboardController;
import com.codeforces.analyzer.model.DashboardStats;

public class DashboardPanel extends JPanel implements Refreshable {
    private final DashboardController dashboardController;
    private final CrawlController crawlController;
    private final JLabel totalUsersLabel = new JLabel("0");
    private final JLabel totalSubmissionsLabel = new JLabel("0");
    private final JLabel aiRateLabel = new JLabel("0%");
    private final JLabel schedulerLabel = new JLabel("Chưa bật");
    private final DefaultTableModel algorithmModel = new DefaultTableModel(new Object[]{"Thuật toán", "Số lần"}, 0);
    private final DefaultTableModel dataStructureModel = new DefaultTableModel(new Object[]{"Cấu trúc dữ liệu", "Số lần"}, 0);

    public DashboardPanel(DashboardController dashboardController, CrawlController crawlController) {
        this.dashboardController = dashboardController;
        this.crawlController = crawlController;
        buildUi();
        refreshData();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel cards = new JPanel(new GridLayout(1, 4, 10, 10));
        cards.add(card("Tổng số nick", totalUsersLabel));
        cards.add(card("Tổng bài nộp", totalSubmissionsLabel));
        cards.add(card("Mức nghi ngờ AI trung bình", aiRateLabel));
        cards.add(card("Thu thập tự động", schedulerLabel));
        add(cards, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
        center.add(tablePanel("Thuật toán phổ biến", algorithmModel));
        center.add(tablePanel("Cấu trúc dữ liệu phổ biến", dataStructureModel));
        add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton refreshButton = new JButton("Làm mới");
        JButton testDbButton = new JButton("Kiểm tra MySQL");
        JButton testGroqButton = new JButton("Kiểm tra Groq");
        JButton startSchedulerButton = new JButton("Bật thu thập mỗi 24 giờ");
        JButton stopSchedulerButton = new JButton("Tắt thu thập tự động");
        JButton crawlAllButton = new JButton("Thu thập tất cả ngay");

        refreshButton.addActionListener(e -> refreshData());
        testDbButton.addActionListener(e -> testDatabase());
        testGroqButton.addActionListener(e -> testGroq());
        startSchedulerButton.addActionListener(e -> {
            crawlController.startScheduler();
            updateSchedulerLabel();
        });
        stopSchedulerButton.addActionListener(e -> {
            crawlController.stopScheduler();
            updateSchedulerLabel();
        });
        crawlAllButton.addActionListener(e -> crawlAllNow());

        actions.add(refreshButton);
        actions.add(testDbButton);
        actions.add(testGroqButton);
        actions.add(startSchedulerButton);
        actions.add(stopSchedulerButton);
        actions.add(crawlAllButton);
        add(actions, BorderLayout.SOUTH);
    }

    private JPanel card(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        valueLabel.setFont(valueLabel.getFont().deriveFont(22f));
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane tablePanel(String title, DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        return scrollPane;
    }

    @Override
    public void refreshData() {
        updateSchedulerLabel();
        new SwingWorker<DashboardStats, Void>() {
            @Override
            protected DashboardStats doInBackground() {
                return dashboardController.getDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    DashboardStats stats = get();
                    totalUsersLabel.setText(String.valueOf(stats.getTotalUsers()));
                    totalSubmissionsLabel.setText(String.valueOf(stats.getTotalSubmissions()));
                    aiRateLabel.setText(String.format("%.2f%%", stats.getAiUsageRate()));
                    fillTable(algorithmModel, stats.getTopAlgorithms());
                    fillTable(dataStructureModel, stats.getTopDataStructures());
                } catch (Exception e) {
                    UiUtils.showError(DashboardPanel.this, "Không tải được tổng quan.", e);
                }
            }
        }.execute();
    }

    private void fillTable(DefaultTableModel model, Map<String, Integer> data) {
        model.setRowCount(0);
        data.forEach((key, value) -> model.addRow(new Object[]{key, value}));
    }

    private void updateSchedulerLabel() {
        schedulerLabel.setText(crawlController.isSchedulerRunning() ? "Đang bật" : "Chưa bật");
    }

    private void testDatabase() {
        boolean success = dashboardController.testDatabase();
        UiUtils.showInfo(this, success ? "Kết nối MySQL thành công." : "Kết nối MySQL thất bại. Hãy kiểm tra cấu hình.");
    }

    private void testGroq() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return dashboardController.testGroqApi();
            }

            @Override
            protected void done() {
                try {
                    UiUtils.showInfo(DashboardPanel.this, get() ? "Kết nối Groq API thành công." : "Kết nối Groq API thất bại.");
                } catch (Exception e) {
                    UiUtils.showError(DashboardPanel.this, "Không kiểm tra được Groq API.", e);
                }
            }
        }.execute();
    }

    private void crawlAllNow() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return crawlController.crawlAllNow();
            }

            @Override
            protected void done() {
                try {
                    UiUtils.showInfo(DashboardPanel.this, "Thu thập tất cả hoàn tất. Bài nộp mới: " + get());
                    refreshData();
                } catch (Exception e) {
                    UiUtils.showError(DashboardPanel.this, "Thu thập tất cả thất bại.", e);
                }
            }
        }.execute();
    }
}
