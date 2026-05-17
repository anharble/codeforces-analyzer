package com.codeforces.analyzer.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.codeforces.analyzer.controller.DashboardController;
import com.codeforces.analyzer.controller.UserController;
import com.codeforces.analyzer.model.DashboardStats;
import com.codeforces.analyzer.model.User;
import com.codeforces.analyzer.model.UserEvaluation;

public class StatisticsPanel extends JPanel implements Refreshable {
    private final UserController userController;
    private final DashboardController dashboardController;
    private final JComboBox<User> userComboBox = new JComboBox<>();
    private final JTextArea evaluationArea = new JTextArea();
    private final BarChartPanel algorithmChart = new BarChartPanel();
    private final DefaultTableModel topUserModel = new DefaultTableModel(new Object[]{"Nick", "Số bài nộp"}, 0);
    private final JLabel dsScoreLabel = new JLabel("0");
    private final JLabel algorithmScoreLabel = new JLabel("0");
    private final JLabel problemScoreLabel = new JLabel("0");
    private final JLabel aiScoreLabel = new JLabel("0%");

    public StatisticsPanel(UserController userController, DashboardController dashboardController) {
        this.userController = userController;
        this.dashboardController = dashboardController;
        buildUi();
        refreshData();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Làm mới thống kê");
        JButton evaluateButton = new JButton("Đánh giá nick");
        refreshButton.addActionListener(e -> refreshData());
        evaluateButton.addActionListener(e -> evaluateSelectedUser());
        top.add(new JLabel("Nick"));
        top.add(userComboBox);
        top.add(evaluateButton);
        top.add(refreshButton);
        add(top, BorderLayout.NORTH);

        JPanel scorePanel = new JPanel(new GridLayout(1, 4, 10, 10));
        scorePanel.add(scoreCard("Điểm CTDL", dsScoreLabel));
        scorePanel.add(scoreCard("Điểm thuật toán", algorithmScoreLabel));
        scorePanel.add(scoreCard("Điểm giải bài", problemScoreLabel));
        scorePanel.add(scoreCard("Nghi ngờ AI", aiScoreLabel));

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(scorePanel, BorderLayout.NORTH);
        evaluationArea.setEditable(false);
        evaluationArea.setLineWrap(true);
        evaluationArea.setWrapStyleWord(true);
        left.add(new JScrollPane(evaluationArea), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.add(algorithmChart, BorderLayout.CENTER);
        JTable topUserTable = new JTable(topUserModel);
        JScrollPane topUserScroll = new JScrollPane(topUserTable);
        topUserScroll.setBorder(BorderFactory.createTitledBorder("Nick có nhiều bài nộp"));
        right.add(topUserScroll, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(0.48);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel scoreCard(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        valueLabel.setFont(valueLabel.getFont().deriveFont(20f));
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void refreshData() {
        DefaultComboBoxModel<User> model = new DefaultComboBoxModel<>();
        for (User user : userController.getAllUsers()) {
            model.addElement(user);
        }
        userComboBox.setModel(model);

        new SwingWorker<DashboardStats, Void>() {
            @Override
            protected DashboardStats doInBackground() {
                return dashboardController.getDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    DashboardStats stats = get();
                    algorithmChart.setData(stats.getTopAlgorithms());
                    topUserModel.setRowCount(0);
                    stats.getTopUsers().forEach((handle, total) -> topUserModel.addRow(new Object[]{handle, total}));
                } catch (Exception e) {
                    UiUtils.showError(StatisticsPanel.this, "Không tải được thống kê.", e);
                }
            }
        }.execute();
        evaluateSelectedUser();
    }

    private void evaluateSelectedUser() {
        User user = (User) userComboBox.getSelectedItem();
        if (user == null) {
            evaluationArea.setText("Chưa có nick để đánh giá.");
            return;
        }
        try {
            UserEvaluation evaluation = dashboardController.evaluateUser(user.getId());
            dsScoreLabel.setText(String.valueOf(evaluation.getDataStructureScore()));
            algorithmScoreLabel.setText(String.valueOf(evaluation.getAlgorithmScore()));
            problemScoreLabel.setText(String.valueOf(evaluation.getProblemSolvingScore()));
            aiScoreLabel.setText(evaluation.getAiUsageScore() + "%");
            evaluationArea.setText(evaluation.getSummary());
        } catch (Exception e) {
            UiUtils.showError(this, "Không đánh giá được nick.", e);
        }
    }
}
