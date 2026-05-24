package restomate.controller;

import restomate.database.DatabaseHelper;
import restomate.model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationController {

    public List<Integer> getTables() {
        List<Integer> tables = new ArrayList<>();
        String query = "SELECT nomor_meja FROM tables ORDER BY nomor_meja ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tables.add(rs.getInt("nomor_meja"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public boolean addTable(int nomorMeja) {
        String query = "INSERT INTO tables (nomor_meja) VALUES (?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, nomorMeja);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteTable(int nomorMeja) {
        String query = "DELETE FROM tables WHERE nomor_meja = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, nomorMeja);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public Map<Integer, Reservation> getActiveReservations() {
        Map<Integer, Reservation> map = new HashMap<>();
        String query = "SELECT * FROM reservations WHERE status IN ('Reserved', 'Dipakai')";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
             
            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("nama_pelanggan");
                int meja = rs.getInt("nomor_meja");
                String tanggal = rs.getString("tanggal_reservasi");
                String jam = rs.getString("jam_reservasi");
                String status = rs.getString("status");
                
                map.put(meja, new Reservation(id, nama, meja, tanggal, jam, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean addReservation(String nama, int meja, String tanggal, String jam) {
        String query = "INSERT INTO reservations (nama_pelanggan, nomor_meja, tanggal_reservasi, jam_reservasi, status) VALUES (?, ?, ?, ?, 'Reserved')";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, nama);
            pstmt.setInt(2, meja);
            pstmt.setString(3, tanggal);
            pstmt.setString(4, jam);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean finishReservation(int meja) {
        String query = "UPDATE reservations SET status = 'Selesai' WHERE nomor_meja = ? AND status IN ('Reserved', 'Dipakai')";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, meja);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void autoUpdateStatus() {
        String query = "UPDATE reservations SET status = 'Dipakai' WHERE status = 'Reserved' AND (tanggal_reservasi < ? OR (tanggal_reservasi = ? AND jam_reservasi <= ?))";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            String currentDate = LocalDate.now().toString();
            String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            
            pstmt.setString(1, currentDate);
            pstmt.setString(2, currentDate);
            pstmt.setString(3, currentTime);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}