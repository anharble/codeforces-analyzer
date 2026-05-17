package com.codeforces.analyzer.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.codeforces.analyzer.controller.AccountController;
import com.codeforces.analyzer.model.CfAccount;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class LoginStatusPanel extends JPanel implements Refreshable {
    private final AccountController accountController;
    private final JLabel accountLabel = new JLabel("Chưa có tài khoản");
    private final JLabel statusLabel = new JLabel("Chưa đăng nhập");
    private final JLabel lastLoginLabel = new JLabel("");
    private final JTextArea noteArea = new JTextArea();

    public LoginStatusPanel(AccountController accountController) {
        this.accountController = accountController;
        buildUi();
        refreshData();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Trạng thái đăng nhập Codeforces"));
        infoPanel.add(new JLabel("Tài khoản đang dùng"));
        infoPanel.add(accountLabel);
        infoPanel.add(new JLabel("Trạng thái"));
        infoPanel.add(statusLabel);
        infoPanel.add(new JLabel("Lần đăng nhập gần nhất"));
        infoPanel.add(lastLoginLabel);
        add(infoPanel, BorderLayout.NORTH);

        noteArea.setEditable(false);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setText("Hệ thống ưu tiên tài khoản đã đăng nhập gần nhất để thu thập mã nguồn Codeforces. Nếu phiên hết hạn, bộ thu thập sẽ tự đăng nhập lại khi có mật khẩu đã lưu.");
        add(noteArea, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Làm mới");
        JButton checkButton = new JButton("Kiểm tra ngay");
        refreshButton.addActionListener(e -> refreshData());
        checkButton.addActionListener(e -> checkNow());
        actions.add(refreshButton);
        actions.add(checkButton);
        add(actions, BorderLayout.SOUTH);
    }

    @Override
    public void refreshData() {
        CfAccount account = accountController.findFirstAvailable();
        if (account == null) {
            accountLabel.setText("Chưa có tài khoản");
            statusLabel.setText("Chưa đăng nhập");
            lastLoginLabel.setText("");
            return;
        }
        accountLabel.setText(account.getUsername());
        statusLabel.setText(account.getStatus());
        lastLoginLabel.setText(DateTimeUtils.format(account.getLastLogin()));
    }

    private void checkNow() {
        CfAccount account = accountController.findFirstAvailable();
        if (account == null) {
            UiUtils.showInfo(this, "Chưa có tài khoản Codeforces để kiểm tra.");
            return;
        }
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return accountController.checkLogin(account);
            }

            @Override
            protected void done() {
                try {
                    UiUtils.showInfo(LoginStatusPanel.this, get() ? "Tài khoản đang đăng nhập." : "Tài khoản chưa đăng nhập hoặc đã hết phiên.");
                    refreshData();
                } catch (Exception e) {
                    UiUtils.showError(LoginStatusPanel.this, "Không kiểm tra được trạng thái đăng nhập.", e);
                    refreshData();
                }
            }
        }.execute();
    }
}
