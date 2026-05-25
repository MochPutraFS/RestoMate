package com.restomate.dao;

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

    // Menambahkan meja baru ke database
    public boolean addTable(int nomorMeja) {
        String query = "INSERT INTO restaurant_tables (nomor_meja) VALUES (?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, nomorMeja);
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
