package com.restomate.dao;

import com.restomate.models.Reservation;
import com.restomate.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    
    // Pake format waktu yang jelas biar nyimpen ke SQLite-nya gampang (SQLite kan ga punya tipe Date beneran).
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Ambil semua data reservasi yang ada.
    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String query = "SELECT * FROM reservations";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                // Parsing string waktu dari SQLite balik jadi LocalDateTime biar java-friendly
                LocalDateTime waktu = null;
                try {
                    waktu = LocalDateTime.parse(rs.getString("waktu_reservasi"), FORMATTER);
                } catch (Exception e) {
                    System.err.println("Waktu reservasi agak beda format nih: " + rs.getString("waktu_reservasi"));
                }
                
                Reservation r = new Reservation(
                    rs.getInt("id"),
                    rs.getString("nama_pelanggan"),
                    rs.getInt("nomor_meja"),
                    waktu,
                    rs.getString("status")
                );
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Gagal narik data reservasi: " + e.getMessage());
        }
        return list;
    }

    // Tambah data bookingan meja baru
    public boolean addReservation(Reservation r) {
        String query = "INSERT INTO reservations (nama_pelanggan, nomor_meja, waktu_reservasi, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setString(1, r.getNamaPelanggan());
            stmt.setInt(2, r.getNomorMeja());
            stmt.setString(3, r.getWaktuReservasi() != null ? r.getWaktuReservasi().format(FORMATTER) : ""); // Ubah format ke String buat masuk DB
            stmt.setString(4, r.getStatus()); // Biasanya kita kasih 'AKTIF'
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Gagal bikin reservasi baru: " + e.getMessage());
            return false;
        }
    }

    // Update status aja, misal dari AKTIF jadi SELESAI atau BATAL
    public boolean updateStatus(int id, String newStatus) {
        String query = "UPDATE reservations SET status = ? WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setString(1, newStatus);
            stmt.setInt(2, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Gagal ngupdate status reservasi: " + e.getMessage());
            return false;
        }
    }

    // Mengecek apakah meja memiliki reservasi aktif (status 'AKTIF')
    public boolean hasActiveReservation(int nomorMeja) {
        String query = "SELECT count(*) FROM reservations WHERE nomor_meja = ? AND status = 'AKTIF'";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, nomorMeja);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal cek reservasi aktif: " + e.getMessage());
        }
        return false;
    }

    // Membatalkan semua reservasi aktif pada meja tertentu
    public boolean cancelReservationsForTable(int nomorMeja) {
        String query = "UPDATE reservations SET status = 'BATAL' WHERE nomor_meja = ? AND status = 'AKTIF'";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, nomorMeja);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Gagal membatalkan reservasi meja: " + e.getMessage());
            return false;
        }
    }
}
