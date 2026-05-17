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
import com.codeforces.analyzer.controller.CrawlController;
import com.codeforces.analyzer.controller.UserController;
import com.codeforces.analyzer.model.Submission;
import com.codeforces.analyzer.model.User;
import com.codeforces.analyzer.service.SubmissionService;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class SubmissionViewerPanel extends JPanel implements Refreshable {
    private final UserController userController;
    private final SubmissionService submissionService;
    private final CrawlController crawlController;
    private final AnalysisController analysisController;
    private final JComboBox<User> userComboBox = new JComboBox<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Mã", "Mã nộp", "Bài làm", "Kết quả", "Ngôn ngữ", "Nộp lúc", "Trạng thái mã nguồn"}, 0);
    private final JTable table = new JTable(tableModel);
    private final JTextArea sourceArea = new JTextArea();
    private final List<Submission> submissions = new ArrayList<>();

    public SubmissionViewerPanel(UserController userController,
                                 SubmissionService submissionService,
                                 CrawlController crawlController,
                                 AnalysisController analysisController) {
        this.userController = userController;
        this.submissionService = submissionService;
        this.crawlController = crawlController;
        this.analysisController = analysisController;
        buildUi();
        refreshData();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadButton = new JButton("Tải bài nộp");
        JButton crawlButton = new JButton("Thu thập ngay");
        JButton analyzeButton = new JButton("Phân tích mã nguồn");
        JButton refreshButton = new JButton("Làm mới");

        loadButton.addActionListener(e -> loadSubmissions());
        crawlButton.addActionListener(e -> crawlSelectedUser());
        analyzeButton.addActionListener(e -> analyzeSelectedSubmission());
        refreshButton.addActionListener(e -> refreshData());

        top.add(new JLabel("Nick"));
        top.add(userComboBox);
        top.add(loadButton);
        top.add(crawlButton);
        top.add(analyzeButton);
        top.add(refreshButton);
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedSource();
            }
        });

        sourceArea.setEditable(false);
        sourceArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        sourceArea.setLineWrap(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), new JScrollPane(sourceArea));
        splitPane.setResizeWeight(0.45);
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
        sourceArea.setText("");
        if (user == null) {
            return;
        }
        submissions.addAll(submissionService.findByUser(user.getId()));
        for (Submission submission : submissions) {
            tableModel.addRow(new Object[]{
                    submission.getId(),
                    submission.getSubmissionId(),
                    submission.getProblemName(),
                    submission.getVerdict(),
                    submission.getLanguage(),
                    DateTimeUtils.format(submission.getSubmitTime()),
                    submission.getSourceCrawlStatus()
            });
        }
    }

    private void crawlSelectedUser() {
        User user = (User) userComboBox.getSelectedItem();
        if (user == null) {
            UiUtils.showInfo(this, "Vui lòng chọn nick cần thu thập.");
            return;
        }
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return crawlController.crawlUser(user);
            }

            @Override
            protected void done() {
                try {
                    UiUtils.showInfo(SubmissionViewerPanel.this, "Thu thập hoàn tất. Bài nộp mới: " + get());
                    loadSubmissions();
                } catch (Exception e) {
                    UiUtils.showError(SubmissionViewerPanel.this, "Thu thập thất bại.", e);
                }
            }
        }.execute();
    }

    private void analyzeSelectedSubmission() {
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
                    UiUtils.showInfo(SubmissionViewerPanel.this, "Đã phân tích mã nguồn.");
                } catch (Exception e) {
                    UiUtils.showError(SubmissionViewerPanel.this, "Phân tích mã nguồn thất bại.", e);
                }
            }
        }.execute();
    }

    private void showSelectedSource() {
        Submission submission = getSelectedSubmission();
        if (submission == null) {
            sourceArea.setText("");
            return;
        }
        if (submission.getSourceCode() == null || submission.getSourceCode().isBlank()) {
            sourceArea.setText("Bài nộp chưa có mã nguồn.\nTrạng thái: " + submission.getSourceCrawlStatus()
                    + "\nGhi chú: " + nullToEmpty(submission.getSourceCrawlMessage()));
        } else {
            sourceArea.setText(submission.getSourceCode());
            sourceArea.setCaretPosition(0);
        }
    }

    private Submission getSelectedSubmission() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= submissions.size()) {
            return null;
        }
        return submissions.get(row);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
