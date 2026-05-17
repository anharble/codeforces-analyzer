package com.codeforces.analyzer.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.codeforces.analyzer.controller.UserController;
import com.codeforces.analyzer.model.Platform;
import com.codeforces.analyzer.model.User;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class UserManagementPanel extends JPanel implements Refreshable {
    private final UserController userController;
    private final JTextField handleField = new JTextField();
    private final JTextField searchField = new JTextField(24);
    private final JComboBox<Platform> platformComboBox = new JComboBox<>(Platform.values());
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[]{"Mã", "Nick", "Nền tảng", "Ngày tạo"}, 0);
    private final JTable table = new JTable(tableModel);
    private final List<User> currentUsers = new ArrayList<>();
    private long selectedUserId;

    public UserManagementPanel(UserController userController) {
        this.userController = userController;
        buildUi();
        refreshData();
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel formPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        JPanel inputPanel = new JPanel(new GridLayout(1, 4, 8, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Thông tin nick"));
        inputPanel.add(new JLabel("Nick"));
        inputPanel.add(handleField);
        inputPanel.add(new JLabel("Nền tảng"));
        inputPanel.add(platformComboBox);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Thêm");
        JButton updateButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");
        JButton clearButton = new JButton("Làm trống");
        JButton searchButton = new JButton("Tìm kiếm");
        JButton refreshButton = new JButton("Làm mới");

        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());
        clearButton.addActionListener(e -> clearForm());
        searchButton.addActionListener(e -> searchUsers());
        refreshButton.addActionListener(e -> refreshData());

        actionPanel.add(addButton);
        actionPanel.add(updateButton);
        actionPanel.add(deleteButton);
        actionPanel.add(clearButton);
        actionPanel.add(new JLabel("Từ khóa"));
        actionPanel.add(searchField);
        actionPanel.add(searchButton);
        actionPanel.add(refreshButton);

        formPanel.add(inputPanel);
        formPanel.add(actionPanel);
        add(formPanel, BorderLayout.NORTH);

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
        loadUsers(userController.getAllUsers());
    }

    private void loadUsers(List<User> users) {
        currentUsers.clear();
        currentUsers.addAll(users);
        tableModel.setRowCount(0);
        for (User user : users) {
            tableModel.addRow(new Object[]{
                    user.getId(),
                    user.getHandle(),
                    user.getPlatform().getTenHienThi(),
                    DateTimeUtils.format(user.getCreatedAt())
            });
        }
    }

    private void addUser() {
        try {
            userController.addUser(handleField.getText(), (Platform) platformComboBox.getSelectedItem());
            UiUtils.showInfo(this, "Đã thêm nick thành công.");
            clearForm();
            refreshData();
        } catch (Exception e) {
            UiUtils.showError(this, "Không thêm được nick.", e);
        }
    }

    private void updateUser() {
        try {
            userController.updateUser(selectedUserId, handleField.getText(), (Platform) platformComboBox.getSelectedItem());
            UiUtils.showInfo(this, "Đã cập nhật nick thành công.");
            clearForm();
            refreshData();
        } catch (Exception e) {
            UiUtils.showError(this, "Không cập nhật được nick.", e);
        }
    }

    private void deleteUser() {
        if (selectedUserId <= 0) {
            UiUtils.showInfo(this, "Vui lòng chọn nick cần xóa.");
            return;
        }
        if (!UiUtils.confirm(this, "Bạn có chắc muốn xóa nick này và toàn bộ bài nộp liên quan?")) {
            return;
        }
        try {
            userController.deleteUser(selectedUserId);
            UiUtils.showInfo(this, "Đã xóa nick thành công.");
            clearForm();
            refreshData();
        } catch (Exception e) {
            UiUtils.showError(this, "Không xóa được nick.", e);
        }
    }

    private void searchUsers() {
        try {
            loadUsers(userController.searchUsers(searchField.getText()));
        } catch (Exception e) {
            UiUtils.showError(this, "Không tìm kiếm được nick.", e);
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentUsers.size()) {
            return;
        }
        User user = currentUsers.get(row);
        selectedUserId = user.getId();
        handleField.setText(user.getHandle());
        platformComboBox.setSelectedItem(user.getPlatform());
    }

    private void clearForm() {
        selectedUserId = 0;
        handleField.setText("");
        table.clearSelection();
    }
}
