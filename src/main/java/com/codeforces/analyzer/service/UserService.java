package com.codeforces.analyzer.service;

import java.sql.SQLException;
import java.util.List;

import com.codeforces.analyzer.dao.UserDAO;
import com.codeforces.analyzer.model.Platform;
import com.codeforces.analyzer.model.User;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User addUser(String handle, Platform platform) {
        validateHandle(handle);
        if (platform == null) {
            throw new IllegalArgumentException("Vui lòng chọn nền tảng.");
        }
        try {
            return userDAO.save(new User(handle.trim(), platform));
        } catch (SQLException e) {
            throw new IllegalStateException("Không thêm được nick. Có thể nick này đã tồn tại.", e);
        }
    }

    public void updateUser(long id, String handle, Platform platform) {
        validateHandle(handle);
        if (id <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn nick cần sửa.");
        }
        try {
            User user = new User(id, handle.trim(), platform, null);
            userDAO.update(user);
        } catch (SQLException e) {
            throw new IllegalStateException("Không cập nhật được nick.", e);
        }
    }

    public void deleteUser(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn nick cần xóa.");
        }
        try {
            userDAO.delete(id);
        } catch (SQLException e) {
            throw new IllegalStateException("Không xóa được nick.", e);
        }
    }

    public List<User> getAllUsers() {
        try {
            return userDAO.findAll();
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được danh sách nick.", e);
        }
    }

    public List<User> searchUsers(String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) {
                return userDAO.findAll();
            }
            return userDAO.search(keyword.trim());
        } catch (SQLException e) {
            throw new IllegalStateException("Không tìm kiếm được nick.", e);
        }
    }

    private void validateHandle(String handle) {
        if (handle == null || handle.isBlank()) {
            throw new IllegalArgumentException("Nick không được để trống.");
        }
        if (handle.length() > 100) {
            throw new IllegalArgumentException("Nick quá dài, tối đa 100 ký tự.");
        }
    }
}
