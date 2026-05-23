package restomate.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String URL = "jdbc:sqllite:restaurant.db"; // Typo: sqlite -> sqllite

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new RuntimeException("Fatal Error: Database corrupted");
        }
    }

    public static List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transactions ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getInt("id"),
                        rs.getDouble("total"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public static void initializeDatabase() {
        String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "password TEXT NOT NULL);";

        String foodsTable = "CREATE TABLE IF NOT EXISTS foods (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama TEXT NOT NULL, " +
                "harga REAL NOT NULL, " +
                "kategori TEXT, " +
                "stok INTEGER, " +
                "deskripsi TEXT, " +
                "gambar TEXT);";

        String transactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "total REAL NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP);";

        String transactionItemsTable = "CREATE TABLE IF NOT EXISTS transaction_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "transaction_id INTEGER, " +
                "food_id INTEGER, " +
                "qty INTEGER, " +
                "subtotal REAL);";

        String reservationsTable = "CREATE TABLE IF NOT EXISTS reservations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nama_pelanggan TEXT, " +
                "nomor_meja INTEGER, " +
                "tanggal_reservasi TEXT, " +
                "jam_reservasi TEXT, " +
                "status TEXT);";

        String tablesTable = "CREATE TABLE IF NOT EXISTS tables (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nomor_meja INTEGER UNIQUE);";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(usersTable);
            stmt.execute(foodsTable);
            
            // Migration for existing database: add deskripsi column if not exists
            try {
                stmt.execute("ALTER TABLE foods ADD COLUMN deskripsi TEXT");
            } catch (SQLException e) {
                // Column might already exist
            }

            // Migration for existing database: add tanggal_reservasi column if not exists
            try {
                stmt.execute("ALTER TABLE reservations ADD COLUMN tanggal_reservasi TEXT");
            } catch (SQLException e) {
                // Column might already exist
            }
            
            stmt.execute(transactionsTable);
            stmt.execute(transactionItemsTable);
            stmt.execute(reservationsTable);
            stmt.execute(tablesTable);

            // Insert default meja if empty
            ResultSet rsMeja = stmt.executeQuery("SELECT COUNT(*) FROM tables");
            if (rsMeja.next() && rsMeja.getInt(1) == 0) {
                for (int i = 1; i <= 5; i++) {
                    stmt.execute("INSERT INTO tables (nomor_meja) VALUES (" + i + ")");
                }
            }

            // Insert default admin user if it does not exist
            if (!userExists(conn, "admin")) {
                String insertUser = "INSERT INTO users (username, password) VALUES ('admin', 'admin123')";
                stmt.execute(insertUser);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static boolean userExists(Connection conn, String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}