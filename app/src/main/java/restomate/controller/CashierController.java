package restomate.controller;

import restomate.database.DatabaseHelper;
import restomate.model.CartItem;
import restomate.model.Makanan;
import restomate.model.MenuRestoran;
import restomate.model.Minuman;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CashierController {

    public List<MenuRestoran> getAvailableFoods() {
        List<MenuRestoran> list = new ArrayList<>();
        String query = "SELECT * FROM foods WHERE stok > 0";
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
                    // Default to Makanan for other categories or "Makanan" specifically
                    list.add(new Makanan(id, nama, harga, stok, deskripsi, kategori, 0));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean processPayment(List<CartItem> cartItems, double totalAmount) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into transactions
            String sqlTx = "INSERT INTO transactions (total) VALUES (?)";
            PreparedStatement psTx = conn.prepareStatement(sqlTx, Statement.RETURN_GENERATED_KEYS);
            psTx.setDouble(1, totalAmount);
            psTx.executeUpdate();
            
            ResultSet rs = psTx.getGeneratedKeys();
            int txId = -1;
            if (rs.next()) {
                txId = rs.getInt(1);
            }

            if (txId == -1) {
                conn.rollback();
                return false;
            }

            // 2. Insert transaction_items & update foods stok
            String sqlItem = "INSERT INTO transaction_items (transaction_id, food_id, qty, subtotal) VALUES (?, ?, ?, ?)";
            String sqlStock = "UPDATE foods SET stok = stok - ? WHERE id = ?";
            
            PreparedStatement psItem = conn.prepareStatement(sqlItem);
            PreparedStatement psStock = conn.prepareStatement(sqlStock);

            for (CartItem item : cartItems) {
                // Transaction item
                psItem.setInt(1, txId);
                psItem.setInt(2, item.getMenu().getId());
                psItem.setInt(3, item.getJumlah());
                psItem.setDouble(4, item.getSubtotal());
                psItem.addBatch();

                // Update stock
                psStock.setInt(1, item.getJumlah());
                psStock.setInt(2, item.getMenu().getId());
                psStock.addBatch();
            }

            psItem.executeBatch();
            psStock.executeBatch();

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
        return false;
    }
}