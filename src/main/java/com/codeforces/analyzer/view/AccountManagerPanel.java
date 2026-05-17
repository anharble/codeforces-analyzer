package com.codeforces.analyzer.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.codeforces.analyzer.controller.AccountController;
import com.codeforces.analyzer.model.CfAccount;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class AccountManagerPanel extends JPanel implements Refreshable {
    private final AccountController accountController;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Mã", "Tài khoản", "Lần đăng nhập", "Trạng thái"}, 0);
    private final JTable table = new JTable(tableModel);
    private final List<CfAccount> accounts = new ArrayList<>();
    private long selectedAccountId;

    public AccountManagerPanel(AccountController accountController) {
        this.accountController = accountController;
        buildUi();
        refreshData();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Tài khoản đăng nhập Codeforces"));
        form.add(new JLabel("Tên đăng nhập hoặc email"));
        form.add(usernameField);
        form.add(new JLabel("Mật khẩu"));
        form.add(passwordField);
        add(form, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Thêm tài khoản");
        JButton updateButton = new JButton("Cập nhật");
        JButton deleteButton = new JButton("Xóa");
        JButton loginButton = new JButton("Đăng nhập");
        JButton checkButton = new JButton("Kiểm tra đăng nhập");
        JButton logoutButton = new JButton("Đăng xuất");
        JButton refreshButton = new JButton("Làm mới");

        addButton.addActionListener(e -> addAccount());
        updateButton.addActionListener(e -> updateAccount());
        deleteButton.addActionListener(e -> deleteAccount());
        loginButton.addActionListener(e -> loginSelected());
        checkButton.addActionListener(e -> checkSelected());
        logoutButton.addActionListener(e -> logoutSelected());
        refreshButton.addActionListener(e -> refreshData());

        actions.add(addButton);
        actions.add(updateButton);
        actions.add(deleteButton);
        actions.add(loginButton);
        actions.add(checkButton);
        actions.add(logoutButton);
        actions.add(refreshButton);
        add(actions, BorderLayout.SOUTH);

        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        accounts.clear();
        accounts.addAll(accountController.getAllAccounts());
        tableModel.setRowCount(0);
        for (CfAccount account : accounts) {
            tableModel.addRow(new Object[]{
                    account.getId(),
                    account.getUsername(),
                    DateTimeUtils.format(account.getLastLogin()),
                    account.getStatus()
            });
        }
    }

    private void addAccount() {
        try {
            accountController.addAccount(usernameField.getText(), String.valueOf(passwordField.getPassword()));
            UiUtils.showInfo(this, "Đã thêm tài khoản Codeforces.");
            clearForm();
            refreshData();
        } catch (Exception e) {
            UiUtils.showError(this, "Không thêm được tài khoản Codeforces.", e);
        }
    }

    private void updateAccount() {
        try {
            accountController.updateAccount(selectedAccountId, usernameField.getText(), String.valueOf(passwordField.getPassword()));
            UiUtils.showInfo(this, "Đã cập nhật tài khoản Codeforces.");
            clearForm();
            refreshData();
        } catch (Exception e) {
            UiUtils.showError(this, "Không cập nhật được tài khoản Codeforces.", e);
        }
    }

    private void deleteAccount() {
        if (selectedAccountId <= 0) {
            UiUtils.showInfo(this, "Vui lòng chọn tài khoản cần xóa.");
            return;
        }
        if (!UiUtils.confirm(this, "Bạn có chắc muốn xóa tài khoản này?")) {
            return;
        }
        try {
            accountController.deleteAccount(selectedAccountId);
            UiUtils.showInfo(this, "Đã xóa tài khoản Codeforces.");
            clearForm();
            refreshData();
        } catch (Exception e) {
            UiUtils.showError(this, "Không xóa được tài khoản Codeforces.", e);
        }
    }

    private void loginSelected() {
        CfAccount account = getSelectedAccount();
        if (account == null) {
            UiUtils.showInfo(this, "Vui lòng chọn tài khoản cần đăng nhập.");
            return;
        }
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return accountController.login(account);
            }

            @Override
            protected void done() {
                try {
                    UiUtils.showInfo(AccountManagerPanel.this, get() ? "Đăng nhập Codeforces thành công." : "Đăng nhập Codeforces thất bại.");
                    refreshData();
                } catch (Exception e) {
                    UiUtils.showError(AccountManagerPanel.this, "Đăng nhập Codeforces thất bại.", e);
                    refreshData();
                }
            }
        }.execute();
    }

    private void checkSelected() {
        CfAccount account = getSelectedAccount();
        if (account == null) {
            UiUtils.showInfo(this, "Vui lòng chọn tài khoản cần kiểm tra.");
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
                    UiUtils.showInfo(AccountManagerPanel.this, get() ? "Tài khoản đang đăng nhập." : "Tài khoản chưa đăng nhập hoặc đã hết phiên.");
                    refreshData();
                } catch (Exception e) {
                    UiUtils.showError(AccountManagerPanel.this, "Không kiểm tra được đăng nhập.", e);
                    refreshData();
                }
            }
        }.execute();
    }

    private void logoutSelected() {
        CfAccount account = getSelectedAccount();
        if (account == null) {
            UiUtils.showInfo(this, "Vui lòng chọn tài khoản cần đăng xuất.");
            return;
        }
        try {
            accountController.logout(account);
            UiUtils.showInfo(this, "Đã đăng xuất tài khoản.");
            refreshData();
        } catch (Exception e) {
            UiUtils.showError(this, "Không đăng xuất được tài khoản.", e);
        }
    }

    private void fillFormFromSelection() {
        CfAccount account = getSelectedAccount();
        if (account == null) {
            return;
        }
        selectedAccountId = account.getId();
        usernameField.setText(account.getUsername());
        passwordField.setText("********");
    }

    private CfAccount getSelectedAccount() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= accounts.size()) {
            return null;
        }
        return accounts.get(row);
    }

    private void clearForm() {
        selectedAccountId = 0;
        usernameField.setText("");
        passwordField.setText("");
        table.clearSelection();
    }
}
