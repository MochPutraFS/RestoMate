package com.restomate.dao;

import com.restomate.models.RestaurantTable;
import com.restomate.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    // Mengambil semua nomor meja dari database secara terurut
    public List<Integer> getAllTableNumbers() {
        List<Integer> list = new ArrayList<>();
        String query = "SELECT nomor_meja FROM restaurant_tables ORDER BY nomor_meja ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getInt("nomor_meja"));
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil nomor meja: " + e.getMessage());
        }
        return list;
    }

    // Mengambil semua objek meja beserta kapasitasnya
    public List<RestaurantTable> getAllTables() {
        List<RestaurantTable> list = new ArrayList<>();
        String query = "SELECT id, nomor_meja, kapasitas FROM restaurant_tables ORDER BY nomor_meja ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new RestaurantTable(
                    rs.getInt("id"),
                    rs.getInt("nomor_meja"),
                    rs.getInt("kapasitas")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil daftar meja: " + e.getMessage());
        }
        return list;
    }

    // Menambahkan meja baru ke database dengan kapasitas default
    public boolean addTable(int nomorMeja) {
        return addTable(nomorMeja, 4); // Default kapasitas 4
    }

    // Menambahkan meja baru ke database dengan kapasitas kustom
    public boolean addTable(int nomorMeja, int kapasitas) {
        String query = "INSERT INTO restaurant_tables (nomor_meja, kapasitas) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, nomorMeja);
            stmt.setInt(2, kapasitas);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal menambahkan meja baru: " + e.getMessage());
            return false;
        }
    }

    // Menghapus meja dari database berdasarkan nomor meja
    public boolean deleteTable(int nomorMeja) {
        String query = "DELETE FROM restaurant_tables WHERE nomor_meja = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, nomorMeja);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal menghapus meja: " + e.getMessage());
            return false;
        }
    }
}
