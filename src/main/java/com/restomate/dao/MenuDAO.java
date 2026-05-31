package com.restomate.dao;

import com.restomate.models.Makanan;
import com.restomate.models.MenuRestoran;
import com.restomate.models.Minuman;
import com.restomate.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    // Ambil semua daftar menu nih, terus kita mapping jadi Makanan atau Minuman. OOP abis pokoknya!
    public List<MenuRestoran> getAllMenus() {
        List<MenuRestoran> list = new ArrayList<>();
        String query = "SELECT * FROM menus";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                String kategori = rs.getString("kategori");
                
                // Kalau MAKANAN, kita bikin instance Makanan. Kalau MINUMAN ya Minuman dong.
                if ("MAKANAN".equalsIgnoreCase(kategori)) {
                    Makanan m = new Makanan(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getDouble("harga"),
                        rs.getInt("stok"),
                        rs.getString("tingkat_pedas")
                    );
                    list.add(m);
                } else if ("MINUMAN".equalsIgnoreCase(kategori)) {
                    Minuman m = new Minuman(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getDouble("harga"),
                        rs.getInt("stok"),
                        rs.getInt("is_dingin") == 1 // Di SQLite kita pakai 1 untuk true, 0 untuk false
                    );
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal ngambil data menu bro: " + e.getMessage());
        }
        return list;
    }

    // Nambah menu baru ke database.
    public boolean addMenu(MenuRestoran menu) {
        String query = "INSERT INTO menus (nama, harga, kategori, stok, tingkat_pedas, is_dingin) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setString(1, menu.getNama());
            stmt.setDouble(2, menu.getHarga());
            stmt.setString(3, menu.getKategori());
            stmt.setInt(4, menu.getStok());
            
            // Cek dulu ini Makanan apa Minuman, biar field spesifiknya gak salah isi
            if (menu instanceof Makanan) {
                Makanan m = (Makanan) menu;
                stmt.setString(5, m.getTingkatPedas());
                stmt.setNull(6, java.sql.Types.INTEGER); // Makanan gak pake is_dingin
            } else if (menu instanceof Minuman) {
                Minuman m = (Minuman) menu;
                stmt.setNull(5, java.sql.Types.VARCHAR); // Minuman gak pedes (kecuali kalau aneh)
                stmt.setInt(6, m.isDingin() ? 1 : 0);
            }
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Gagal nambah menu: " + e.getMessage());
            return false;
        }
    }

    // Update menu yang udah ada. Hampir sama kayak nambah, bedanya pake UPDATE SET aja.
    public boolean updateMenu(MenuRestoran menu) {
        String query = "UPDATE menus SET nama = ?, harga = ?, kategori = ?, stok = ?, tingkat_pedas = ?, is_dingin = ? WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
             
            stmt.setString(1, menu.getNama());
            stmt.setDouble(2, menu.getHarga());
            stmt.setString(3, menu.getKategori());
            stmt.setInt(4, menu.getStok());
            
            if (menu instanceof Makanan) {
                Makanan m = (Makanan) menu;
                stmt.setString(5, m.getTingkatPedas());
                stmt.setNull(6, java.sql.Types.INTEGER);
            } else if (menu instanceof Minuman) {
                Minuman m = (Minuman) menu;
                stmt.setNull(5, java.sql.Types.VARCHAR);
                stmt.setInt(6, m.isDingin() ? 1 : 0);
            }
            
            // Jangan lupa ID-nya buat klausa WHERE
            stmt.setInt(7, menu.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Gagal update menu: " + e.getMessage());
            return false;
        }
    }

    // Hapus menu dari kehidupan kita selamanya~ (dan dari database)
    // PENTING: Kita update dulu foreign key di riwayat transaksi jadi NULL biar gak kena error Constraint Violation
    public boolean deleteMenu(int id) {
        String updateFkQuery = "UPDATE transaction_items SET menu_id = NULL WHERE menu_id = ?";
        String deleteQuery = "DELETE FROM menus WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Bebaskan dulu ikatan foreign key dari transaksi lama
            try (PreparedStatement stmtUpdate = conn.prepareStatement(updateFkQuery)) {
                stmtUpdate.setInt(1, id);
                stmtUpdate.executeUpdate();
            }
            
            // 2. Baru deh hapus menunya dengan aman
            boolean success = false;
            try (PreparedStatement stmtDelete = conn.prepareStatement(deleteQuery)) {
                stmtDelete.setInt(1, id);
                success = stmtDelete.executeUpdate() > 0;
            }
            
            if (success) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return success;
            
        } catch (SQLException e) {
            System.err.println("Gagal hapus menu: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { }
            }
        }
    }

    // Mengimpor daftar menu secara massal dalam satu transaksi database (All-or-Nothing)
    public boolean importMenus(List<MenuRestoran> menus) {
        String query = "INSERT INTO menus (nama, harga, kategori, stok, tingkat_pedas, is_dingin) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                for (MenuRestoran menu : menus) {
                    stmt.setString(1, menu.getNama());
                    stmt.setDouble(2, menu.getHarga());
                    stmt.setString(3, menu.getKategori());
                    stmt.setInt(4, menu.getStok());
                    
                    if (menu instanceof Makanan) {
                        Makanan m = (Makanan) menu;
                        stmt.setString(5, m.getTingkatPedas());
                        stmt.setNull(6, java.sql.Types.INTEGER);
                    } else if (menu instanceof Minuman) {
                        Minuman m = (Minuman) menu;
                        stmt.setNull(5, java.sql.Types.VARCHAR);
                        stmt.setInt(6, m.isDingin() ? 1 : 0);
                    }
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Gagal mengimpor menu secara massal: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { }
            }
        }
    }
}
