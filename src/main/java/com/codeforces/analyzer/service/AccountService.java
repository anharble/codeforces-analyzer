package com.codeforces.analyzer.service;

import java.sql.SQLException;
import java.util.List;

import com.codeforces.analyzer.dao.CfAccountDAO;
import com.codeforces.analyzer.model.CfAccount;
import com.codeforces.analyzer.utils.PasswordUtil;

public class AccountService {
    private final CfAccountDAO accountDAO;

    public AccountService(CfAccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public CfAccount addAccount(String username, String password) {
        validateUsername(username);
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        CfAccount account = new CfAccount();
        account.setUsername(username.trim());
        account.setEncryptedPassword(PasswordUtil.encrypt(password));
        account.setStatus("Chưa đăng nhập");
        try {
            return accountDAO.save(account);
        } catch (SQLException e) {
            throw new IllegalStateException("Không lưu được tài khoản Codeforces.", e);
        }
    }

    public void updateAccount(long id, String username, String passwordOrMask) {
        validateUsername(username);
        try {
            CfAccount account = accountDAO.findById(id);
            if (account == null) {
                throw new IllegalArgumentException("Không tìm thấy tài khoản cần sửa.");
            }
            account.setUsername(username.trim());
            if (passwordOrMask != null && !passwordOrMask.isBlank() && !"********".equals(passwordOrMask)) {
                account.setEncryptedPassword(PasswordUtil.encrypt(passwordOrMask));
            }
            accountDAO.update(account);
        } catch (SQLException e) {
            throw new IllegalStateException("Không cập nhật được tài khoản Codeforces.", e);
        }
    }

    public void deleteAccount(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn tài khoản cần xóa.");
        }
        try {
            accountDAO.delete(id);
        } catch (SQLException e) {
            throw new IllegalStateException("Không xóa được tài khoản Codeforces.", e);
        }
    }

    public List<CfAccount> getAllAccounts() {
        try {
            return accountDAO.findAll();
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được danh sách tài khoản Codeforces.", e);
        }
    }

    public CfAccount findById(long id) {
        try {
            return accountDAO.findById(id);
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được tài khoản Codeforces.", e);
        }
    }

    public CfAccount findFirstAvailable() {
        try {
            return accountDAO.findFirstAvailable();
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được tài khoản đăng nhập.", e);
        }
    }

    public String decryptPassword(CfAccount account) {
        return PasswordUtil.decrypt(account.getEncryptedPassword());
    }

    public void updateSession(long id, String cookies, String status) {
        try {
            accountDAO.updateSession(id, cookies, status);
        } catch (SQLException e) {
            throw new IllegalStateException("Không lưu được phiên đăng nhập Codeforces.", e);
        }
    }

    public void updateStatus(long id, String status) {
        try {
            accountDAO.updateStatus(id, status);
        } catch (SQLException e) {
            throw new IllegalStateException("Không cập nhật được trạng thái đăng nhập.", e);
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc email không được để trống.");
        }
    }
}
