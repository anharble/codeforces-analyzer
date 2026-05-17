package com.codeforces.analyzer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.codeforces.analyzer.database.DatabaseConnection;
import com.codeforces.analyzer.model.Platform;
import com.codeforces.analyzer.model.User;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class UserDAO {
    public User save(User user) throws SQLException {
        String sql = "INSERT INTO users(handle, platform) VALUES(?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getHandle());
            statement.setString(2, user.getPlatform().name());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getLong(1));
                }
            }
            return user;
        }
    }

    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET handle = ?, platform = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getHandle());
            statement.setString(2, user.getPlatform().name());
            statement.setLong(3, user.getId());
            statement.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, handle, platform, created_at FROM users ORDER BY created_at DESC, handle";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
            return users;
        }
    }

    public List<User> search(String keyword) throws SQLException {
        String sql = "SELECT id, handle, platform, created_at FROM users WHERE handle LIKE ? ORDER BY handle";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + keyword + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
                return users;
            }
        }
    }

    public User findById(long id) throws SQLException {
        String sql = "SELECT id, handle, platform, created_at FROM users WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
                return null;
            }
        }
    }

    public long countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("handle"),
                Platform.valueOf(resultSet.getString("platform")),
                DateTimeUtils.fromTimestamp(resultSet.getTimestamp("created_at"))
        );
    }
}
