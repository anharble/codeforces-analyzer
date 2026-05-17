package com.codeforces.analyzer.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

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

import com.codeforces.analyzer.controller.AnalysisController;
import com.codeforces.analyzer.controller.UserController;
import com.codeforces.analyzer.model.AnalysisResult;
import com.codeforces.analyzer.model.Submission;
import com.codeforces.analyzer.model.User;
import com.codeforces.analyzer.service.StatisticsService;
import com.codeforces.analyzer.service.SubmissionService;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class AIAnalysisPanel extends JPanel implements Refreshable {
    private final UserController userController;
    private final SubmissionService submissionService;
    private final AnalysisController analysisController;
    private final StatisticsService statisticsService;
    private final JComboBox<User> userComboBox = new JComboBox<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Mã", "Mã nộp", "Bài làm", "Đã phân tích", "Mức nghi ngờ AI"}, 0);
    private final JTable table = new JTable(tableModel);
    private final JTextArea detailArea = new JTextArea();
    private final List<Submission> submissions = new ArrayList<>();

    public AIAnalysisPanel(UserController userController,
                           SubmissionService submissionService,
                           AnalysisController analysisController,
                           StatisticsService statisticsService) {
        this.userController = userController;
        this.submissionService = submissionService;
        this.analysisController = analysisController;
        this.statisticsService = statisticsService;
        buildUi();
        refreshData();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadButton = new JButton("Tải dữ liệu");
        JButton analyzeButton = new JButton("Phân tích đang chọn");
        JButton analyzeAllButton = new JButton("Phân tích tất cả chưa phân tích");

        loadButton.addActionListener(e -> loadSubmissions());
        analyzeButton.addActionListener(e -> analyzeSelected());
        analyzeAllButton.addActionListener(e -> analyzeAllPending());

        top.add(new JLabel("Nick"));
        top.add(userComboBox);
        top.add(loadButton);
        top.add(analyzeButton);
        top.add(analyzeAllButton);
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedAnalysis();
            }
        });

        detailArea.setEditable(false);
        detailArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), new JScrollPane(detailArea));
        splitPane.setResizeWeight(0.50);
        add(splitPane, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        DefaultComboBoxModel<User> model = new DefaultComboBoxModel<>();
        for (User user : userController.getAllUsers()) {
            model.addElement(user);
        }
        userComboBox.setModel(model);
        loadSubmissions();
    }

    private void loadSubmissions() {
        User user = (User) userComboBox.getSelectedItem();
        submissions.clear();
        tableModel.setRowCount(0);
        detailArea.setText("");
        if (user == null) {
            return;
        }
        submissions.addAll(submissionService.findByUser(user.getId()));
        for (Submission submission : submissions) {
            AnalysisResult result = analysisController.findBySubmissionId(submission.getId());
            tableModel.addRow(new Object[]{
                    submission.getId(),
                    submission.getSubmissionId(),
                    submission.getProblemName(),
                    result == null ? "Chưa" : "Rồi",
                    result == null ? "" : result.getAiProbability() + "%"
            });
        }
    }

    private void analyzeSelected() {
        Submission submission = getSelectedSubmission();
        if (submission == null) {
            UiUtils.showInfo(this, "Vui lòng chọn bài nộp cần phân tích.");
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                analysisController.analyzeSubmission(submission.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UiUtils.showInfo(AIAnalysisPanel.this, "Đã phân tích bài nộp.");
                    loadSubmissions();
                } catch (Exception e) {
                    UiUtils.showError(AIAnalysisPanel.this, "Phân tích thất bại.", e);
                }
            }
        }.execute();
    }

    private void analyzeAllPending() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return analysisController.analyzeAllPending().size();
            }

            @Override
            protected void done() {
                try {
                    UiUtils.showInfo(AIAnalysisPanel.this, "Đã phân tích thêm " + get() + " bài nộp.");
                    loadSubmissions();
                } catch (Exception e) {
                    UiUtils.showError(AIAnalysisPanel.this, "Phân tích hàng loạt thất bại.", e);
                }
            }
        }.execute();
    }

    private void showSelectedAnalysis() {
        Submission submission = getSelectedSubmission();
        if (submission == null) {
            detailArea.setText("");
            return;
        }
        AnalysisResult result = analysisController.findBySubmissionId(submission.getId());
        if (result == null) {
            detailArea.setText("Bài nộp này chưa được phân tích.");
            return;
        }
        detailArea.setText(formatAnalysis(result));
        detailArea.setCaretPosition(0);
    }

    private String formatAnalysis(AnalysisResult result) {
        return "Thời gian phân tích: " + DateTimeUtils.format(result.getAnalysisTime())
                + "\nThuật toán: " + String.join(", ", statisticsService.readItems(result.getDetectedAlgorithms()))
                + "\nCấu trúc dữ liệu: " + String.join(", ", statisticsService.readItems(result.getDetectedDataStructures()))
                + "\nĐộ khó tư duy: " + result.getDifficultyLevel()
                + "\nĐiểm thuật toán: " + result.getAlgorithmScore()
                + "\nĐiểm cấu trúc dữ liệu: " + result.getDataStructureScore()
                + "\nMức nghi ngờ AI: " + result.getAiProbability() + "%"
                + "\nĐộ tin cậy: " + result.getAiConfidence()
                + "\nLý do nghi ngờ: " + String.join("; ", statisticsService.readItems(result.getAiReasons()))
                + "\n\nNhận xét:\n" + result.getAiComment();
    }

    private Submission getSelectedSubmission() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= submissions.size()) {
            return null;
        }
        return submissions.get(row);
    }
}
