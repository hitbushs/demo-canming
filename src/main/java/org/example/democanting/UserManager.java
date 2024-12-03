package org.example.democanting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserManager {

    public int register(String username, String password) {
        String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, 'user')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // 返回新注册用户的ID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 注册失败
    }

    public int login(String username, String password) {
        String sql = "SELECT id FROM Users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id"); // 返回登录用户的ID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 登录失败
    }

    public boolean isAdmin(int userId) {
        String sql = "SELECT role FROM Users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return "admin".equals(rs.getString("role")); // 检查用户角色是否为管理员
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // 默认返回false
    }
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE Users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}