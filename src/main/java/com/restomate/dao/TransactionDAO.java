package com.restomate.dao;

import com.restomate.models.Transaction;
import com.restomate.models.TransactionItem;
import com.restomate.utils.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class TransactionDAO {

    // Menyimpan transaksi utama beserta rincian belanjaannya. 
    // Kita pake transaction database beneran (commit & rollback) biar kalo ada error di tengah jalan, datanya gak setengah-setengah masuknya.
    public boolean saveTransaction(Transaction transaction, List<TransactionItem> items) {
        String queryTx = "INSERT INTO transactions (total, metode_pembayaran, catatan, nama_pelanggan, nomor_antrian, tipe_pesanan) VALUES (?, ?, ?, ?, ?, ?)";
        String queryItem = "INSERT INTO transaction_items (transaction_id, menu_id, qty, subtotal) VALUES (?, ?, ?, ?)";
        String queryUpdateStok = "UPDATE menus SET stok = stok - ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            
            // Matikan auto-commit! Kita mau ngontrol sendiri kapan datanya beneran disimpen
            conn.setAutoCommit(false);
            
            int transactionId = -1;
            
            // 1. Insert ke tabel transactions dulu, lalu ambil ID yang baru aja terbuat
            try (PreparedStatement stmtTx = conn.prepareStatement(queryTx, Statement.RETURN_GENERATED_KEYS)) {
                stmtTx.setDouble(1, transaction.getTotal());
                stmtTx.setString(2, transaction.getMetodePembayaran());
                stmtTx.setString(3, transaction.getCatatan());
                stmtTx.setString(4, transaction.getNamaPelanggan());
                stmtTx.setString(5, transaction.getNomorAntrian());
                stmtTx.setString(6, transaction.getTipePesanan());
                stmtTx.executeUpdate();
                
                try (ResultSet rs = stmtTx.getGeneratedKeys()) {
                    if (rs.next()) {
                        transactionId = rs.getInt(1);
                    } else {
                        throw new SQLException("Waduh, gagal dapet transaction_id nih!");
                    }
                }
            }
            
            // 2. Insert detail barang beliannya ke tabel transaction_items dan potong stoknya
            try (PreparedStatement stmtItem = conn.prepareStatement(queryItem);
                 PreparedStatement stmtUpdateStok = conn.prepareStatement(queryUpdateStok)) {
                 
                for (TransactionItem item : items) {
                    // Set item transaksi
                    stmtItem.setInt(1, transactionId);
                    stmtItem.setInt(2, item.getMenuId());
                    stmtItem.setInt(3, item.getQty());
                    stmtItem.setDouble(4, item.getSubtotal());
                    stmtItem.addBatch(); // Kita tumpuk dulu perintahnya
                    
                    // Set pemotongan stok
                    stmtUpdateStok.setInt(1, item.getQty());
                    stmtUpdateStok.setInt(2, item.getMenuId());
                    stmtUpdateStok.addBatch(); // Ditumpuk juga
                }
                
                // Eksekusi tumpukan perintah sekaligus, lebih cepet cuy!
                stmtItem.executeBatch();
                stmtUpdateStok.executeBatch();
            }
            
            // 3. Semua mulus tanpa error? Sip, kita commit biar resmi tersimpan!
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Gagal nyimpen transaksi, kita rollback ya: " + e.getMessage());
            if (conn != null) {
                try {
                    // Oh tidak, ada yang error! Balikin semua data kaya semula (rollback).
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Wadaw, gagal rollback juga: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            // Walaupun berhasil atau error, pastikan auto-commit dinyalain lagi dan koneksinya ditutup
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Gagal nutup koneksi: " + e.getMessage());
                }
            }
        }
    }

    // Ambil total omset (pendapatan) hari ini aja.
    public double getTodayTotalIncome() {
        // Query asik buat ngambil totalan cuan dari transaksi yang tanggalnya sama dengan hari ini.
        String query = "SELECT SUM(total) FROM transactions WHERE DATE(created_at) = CURRENT_DATE";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
             
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Gagal ngitung duit hari ini: " + e.getMessage());
        }
        return 0.0;
    }

    // Hitung total jumlah transaksi hari ini
    public int getTodayTransactionCount() {
        String query = "SELECT COUNT(*) FROM transactions WHERE DATE(created_at) = CURRENT_DATE";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Gagal ngitung jumlah transaksi hari ini: " + e.getMessage());
        }
        return 0;
    }

    // Hitung total pendapatan hari ini berdasarkan metode pembayaran (CASH/QRIS)
    public double getTodayIncomeByMethod(String method) {
        String query = "SELECT SUM(total) FROM transactions WHERE DATE(created_at) = CURRENT_DATE AND metode_pembayaran = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, method);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal ngitung pendapatan " + method + " hari ini: " + e.getMessage());
        }
        return 0.0;
    }

    // Mengambil seluruh riwayat transaksi hari ini
    public List<Transaction> getTodayTransactions() {
        List<Transaction> list = new java.util.ArrayList<>();
        String query = "SELECT * FROM transactions WHERE DATE(created_at) = CURRENT_DATE ORDER BY created_at DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                java.time.LocalDateTime time = null;
                String timeStr = rs.getString("created_at");
                if (timeStr != null) {
                    try {
                        time = java.time.LocalDateTime.parse(timeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (Exception e) {
                        try {
                            time = java.time.LocalDateTime.parse(timeStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        } catch (Exception ignore) {}
                    }
                }
                Transaction t = new Transaction(
                    rs.getInt("id"),
                    rs.getDouble("total"),
                    rs.getString("metode_pembayaran"),
                    rs.getString("catatan"),
                    time
                );
                t.setNamaPelanggan(rs.getString("nama_pelanggan"));
                t.setNomorAntrian(rs.getString("nomor_antrian"));
                t.setTipePesanan(rs.getString("tipe_pesanan"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Gagal ngambil transaksi hari ini: " + e.getMessage());
        }
        return list;
    }

    // Mengambil seluruh riwayat transaksi dalam rentang tanggal tertentu
    public List<Transaction> getTransactionsRange(String startDate, String endDate) {
        List<Transaction> list = new java.util.ArrayList<>();
        String query = "SELECT * FROM transactions WHERE DATE(created_at) >= ? AND DATE(created_at) <= ? ORDER BY created_at DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.time.LocalDateTime time = null;
                    String timeStr = rs.getString("created_at");
                    if (timeStr != null) {
                        try {
                            time = java.time.LocalDateTime.parse(timeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        } catch (Exception e) {
                            try {
                                time = java.time.LocalDateTime.parse(timeStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            } catch (Exception ignore) {}
                        }
                    }
                    Transaction t = new Transaction(
                        rs.getInt("id"),
                        rs.getDouble("total"),
                        rs.getString("metode_pembayaran"),
                        rs.getString("catatan"),
                        time
                    );
                    t.setNamaPelanggan(rs.getString("nama_pelanggan"));
                    t.setNomorAntrian(rs.getString("nomor_antrian"));
                    t.setTipePesanan(rs.getString("tipe_pesanan"));
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal ngambil transaksi rentang tanggal: " + e.getMessage());
        }
        return list;
    }
}
