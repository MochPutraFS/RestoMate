package restomate.controller;

import restomate.database.DatabaseHelper;
import restomate.model.Makanan;
import restomate.model.MenuRestoran;
import restomate.model.Minuman;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodController {

    public List<MenuRestoran> getAllFoods() {
        List<MenuRestoran> list = new ArrayList<>();
        String query = "SELECT * FROM foods";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("nama");
                double harga = rs.getDouble("harga");
                String kategori = rs.getString("kategori");
                int stok = rs.getInt("stok");
                String deskripsi = rs.getString("deskripsi"); 
                
                if ("Minuman".equalsIgnoreCase(kategori)) {
                    list.add(new Minuman(id, nama, harga, stok, deskripsi, kategori, false));
                } else {
                    list.add(new Makanan(id, nama, harga, stok, deskripsi, kategori, 0));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addFood(MenuRestoran menu) {
        String query = "INSERT INTO foods (nama, harga, kategori, stok, deskripsi) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, menu.getNama());
            pstmt.setDouble(2, menu.getHarga());
            pstmt.setString(3, menu.getKategori());
            pstmt.setInt(4, menu.getStok());
            pstmt.setString(5, menu.getDeskripsi()); 
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateFood(MenuRestoran menu) {
        String query = "UPDATE foods SET nama = ?, harga = ?, kategori = ?, stok = ?, deskripsi = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, menu.getNama());
            pstmt.setDouble(2, menu.getHarga());
            pstmt.setString(3, menu.getKategori());
            pstmt.setInt(4, menu.getStok());
            pstmt.setString(5, menu.getDeskripsi()); 
            pstmt.setInt(6, menu.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteFood(int id) {
        String query = "DELETE FROM foods WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}