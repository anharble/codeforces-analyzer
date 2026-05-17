package com.codeforces.analyzer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.codeforces.analyzer.database.DatabaseConnection;
import com.codeforces.analyzer.model.CfAccount;
import com.codeforces.analyzer.utils.DateTimeUtils;

public class CfAccountDAO {
    public CfAccount save(CfAccount account) throws SQLException {
        String sql = "INSERT INTO cf_accounts(username, encrypted_password, status) VALUES(?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, account.getUsername());
            statement.setString(2, account.getEncryptedPassword());
            statement.setString(3, account.getStatus());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    account.setId(resultSet.getLong(1));
                }
            }
            return account;
        }
    }

    public void update(CfAccount account) throws SQLException {
        String sql = """
                UPDATE cf_accounts
                SET username = ?, encrypted_password = ?, status = ?, cookies = ?, last_login = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, account.getUsername());
            statement.setString(2, account.getEncryptedPassword());
            statement.setString(3, account.getStatus());
            statement.setString(4, account.getCookies());
            statement.setTimestamp(5, DateTimeUtils.toTimestamp(account.getLastLogin()));
            statement.setLong(6, account.getId());
            statement.executeUpdate();
        }
    }

    public void updateSession(long id, String cookies, String status) throws SQLException {
        String sql = "UPDATE cf_accounts SET cookies = ?, status = ?, last_login = NOW() WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, cookies);
            statement.setString(2, status);
            statement.setLong(3, id);
            statement.executeUpdate();
        }
    }

    public void updateStatus(long id, String status) throws SQLException {
        String sql = "UPDATE cf_accounts SET status = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM cf_accounts WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public List<CfAccount> findAll() throws SQLException {
        String sql = "SELECT * FROM cf_accounts ORDER BY updated_at DESC, username";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<CfAccount> accounts = new ArrayList<>();
            while (resultSet.next()) {
                accounts.add(mapAccount(resultSet));
            }
            return accounts;
        }
    }

    public CfAccount findById(long id) throws SQLException {
        String sql = "SELECT * FROM cf_accounts WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAccount(resultSet);
                }
                return null;
            }
        }
    }

    public CfAccount findFirstAvailable() throws SQLException {
        String sql = """
                SELECT * FROM cf_accounts
                ORDER BY CASE WHEN status = 'Đã đăng nhập' THEN 0 ELSE 1 END, last_login DESC, id ASC
                LIMIT 1
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return mapAccount(resultSet);
            }
            return null;
        }
    }

    private CfAccount mapAccount(ResultSet resultSet) throws SQLException {
        CfAccount account = new CfAccount();
        account.setId(resultSet.getLong("id"));
        account.setUsername(resultSet.getString("username"));
        account.setEncryptedPassword(resultSet.getString("encrypted_password"));
        account.setLastLogin(DateTimeUtils.fromTimestamp(resultSet.getTimestamp("last_login")));
        account.setCookies(resultSet.getString("cookies"));
        account.setStatus(resultSet.getString("status"));
        return account;
    }
}
