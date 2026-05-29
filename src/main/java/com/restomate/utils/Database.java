package com.restomate.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Database {
    private static final String URL = "jdbc:sqlite:restaurant.db?busy_timeout=3000";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS menus (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nama TEXT NOT NULL, " +
                    "harga REAL NOT NULL, " +
                    "kategori TEXT NOT NULL, " +
                    "stok INTEGER DEFAULT 0, " +
                    "gambar TEXT, " +
                    "tingkat_pedas TEXT, " +
                    "is_dingin INTEGER DEFAULT 0" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "total REAL NOT NULL, " +
                    "metode_pembayaran TEXT NOT NULL, " +
                    "catatan TEXT, " +
                    "nama_pelanggan TEXT, " +
                    "nomor_antrian TEXT, " +
                    "tipe_pesanan TEXT DEFAULT 'DINE IN', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");
                    
            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN catatan TEXT;");
            } catch (SQLException ignore) { }

            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN nama_pelanggan TEXT;");
            } catch (SQLException ignore) { }

            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN nomor_antrian TEXT;");
            } catch (SQLException ignore) { }

            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN tipe_pesanan TEXT DEFAULT 'DINE IN';");
            } catch (SQLException ignore) { }

            stmt.execute("CREATE TABLE IF NOT EXISTS transaction_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "transaction_id INTEGER, " +
                    "menu_id INTEGER, " +
                    "qty INTEGER NOT NULL, " +
                    "subtotal REAL NOT NULL, " +
                    "FOREIGN KEY(transaction_id) REFERENCES transactions(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(menu_id) REFERENCES menus(id)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nama_pelanggan TEXT NOT NULL, " +
                    "nomor_meja INTEGER NOT NULL, " +
                    "waktu_reservasi TEXT NOT NULL, " +
                    "status TEXT DEFAULT 'AKTIF', " +
                    "jumlah_orang INTEGER DEFAULT 1, " +
                    "menu_dipesan TEXT, " +
                    "catatan TEXT, " +
                    "waktu_siap TEXT, " +
                    "biaya_total REAL DEFAULT 0, " +
                    "dp_dibayar REAL DEFAULT 0" +
                    ");");

            try {
                stmt.execute("ALTER TABLE reservations ADD COLUMN jumlah_orang INTEGER DEFAULT 1;");
            } catch (SQLException ignore) { }

            try {
                stmt.execute("ALTER TABLE reservations ADD COLUMN menu_dipesan TEXT;");
            } catch (SQLException ignore) { }

            try {
                stmt.execute("ALTER TABLE reservations ADD COLUMN catatan TEXT;");
            } catch (SQLException ignore) { }

            try {
                stmt.execute("ALTER TABLE reservations ADD COLUMN waktu_siap TEXT;");
            } catch (SQLException ignore) { }

            try {
                stmt.execute("ALTER TABLE reservations ADD COLUMN biaya_total REAL DEFAULT 0;");
            } catch (SQLException ignore) { }

            try {
                stmt.execute("ALTER TABLE reservations ADD COLUMN dp_dibayar REAL DEFAULT 0;");
            } catch (SQLException ignore) { }

            stmt.execute("CREATE TABLE IF NOT EXISTS restaurant_tables (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nomor_meja INTEGER NOT NULL UNIQUE, " +
                    "kapasitas INTEGER DEFAULT 4" +
                    ");");

            try {
                stmt.execute("ALTER TABLE restaurant_tables ADD COLUMN kapasitas INTEGER DEFAULT 4;");
            } catch (SQLException ignore) { }

            try {
                var rsMeja = stmt.executeQuery("SELECT count(*) FROM restaurant_tables");
                if (rsMeja.next() && rsMeja.getInt(1) == 0) {
                    for (int i = 1; i <= 12; i++) {
                        stmt.execute("INSERT INTO restaurant_tables (nomor_meja, kapasitas) VALUES (" + i + ", 4)");
                    }
                    System.out.println("Default 12 tables seeded.");
                }
            } catch (SQLException ignore) { }

            try {
                var rs = stmt.executeQuery("SELECT count(*) FROM users WHERE username = 'admin'");
                if (rs.next() && rs.getInt(1) == 0) {
                    String hashedPassword = hashPassword("admin");
                    stmt.execute("INSERT INTO users (username, password) VALUES ('admin', '" + hashedPassword + "')");
                    System.out.println("Default admin user created.");
                }
            } catch (Exception e) {
                System.err.println("Failed to insert default admin: " + e.getMessage());
            }

            System.out.println("Database initialization completed.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
