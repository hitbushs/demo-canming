package org.example.democanting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationManager {

    public boolean makeReservation(int userId, int restaurantId, String date, String time, int seats) {
        String sql = "INSERT INTO Reservations (user_id, restaurant_id, reservation_date, reservation_time, number_of_seats) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, restaurantId);
            pstmt.setString(3, date);
            pstmt.setString(4, time);
            pstmt.setInt(5, seats);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getReservationsByUserId(int userId) {
        List<String> reservations = new ArrayList<>();
        String sql = "SELECT r.id, res.name, r.reservation_date, r.reservation_time FROM Reservations r JOIN Restaurants res ON r.restaurant_id = res.id WHERE r.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String reservation = "Reservation ID: " + rs.getInt("id") +
                        ", Restaurant: " + rs.getString("name") +
                        ", Date: " + rs.getString("reservation_date") +
                        ", Time: " + rs.getString("reservation_time");
                reservations.add(reservation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    public boolean cancelReservation(int reservationId) {
        String sql = "DELETE FROM Reservations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}