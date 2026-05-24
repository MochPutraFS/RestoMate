package restomate.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import restomate.database.DatabaseHelper;
import restomate.model.Transaction;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportView {
    private VBox view;
    private TableView<Transaction> table;
    private ObservableList<Transaction> transactionList;

    public ReportView() {
        createView();
        loadData();
    }

    private void createView() {
        view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #F4F7FC;");

        Label titleLabel = new Label("Laporan Transaksi Harian");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2C3E50;");

        table = new TableView<>();
        
        TableColumn<Transaction, Integer> colId = new TableColumn<>("ID Transaksi");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(100);

        TableColumn<Transaction, Double> colTotal = new TableColumn<>("Total Harga (Rp)");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(200);

        TableColumn<Transaction, String> colDate = new TableColumn<>("Tanggal/Jam");
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colDate.setPrefWidth(250);

        table.getColumns().addAll(colId, colTotal, colDate);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        Button btnPrint = new Button("Cetak Laporan Harian");
        btnPrint.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px;");
        btnPrint.setOnAction(e -> handlePrint());

        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.getChildren().add(btnPrint);

        view.getChildren().addAll(titleLabel, table, actionBox);
    }

    private void loadData() {
        List<Transaction> data = DatabaseHelper.getAllTransactions();
        transactionList = FXCollections.observableArrayList(data);
        table.setItems(transactionList);
    }

    private void handlePrint() {
        if (transactionList == null || transactionList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Tidak ada data transaksi untuk dicetak.");
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "laporan_harian_" + timestamp + ".txt";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("========================================\n");
            writer.write("         LAPORAN TRANSAKSI HARIAN       \n");
            writer.write("========================================\n");
            writer.write("Waktu Cetak: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("----------------------------------------\n");
            writer.write(String.format("%-5s | %-15s | %-20s\n", "ID", "Total (Rp)", "Tanggal/Jam"));
            writer.write("----------------------------------------\n");

            double grandTotal = 0;
            for (Transaction tx : transactionList) {
                writer.write(String.format("%-5d | %-15.2f | %-20s\n", 
                        tx.getId(), tx.getTotal(), tx.getCreatedAt()));
                grandTotal += tx.getTotal();
            }

            writer.write("----------------------------------------\n");
            writer.write(String.format("GRAND TOTAL:         Rp %15.2f\n", grandTotal));
            writer.write("========================================\n");
            
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Laporan berhasil dicetak ke file: " + fileName);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menulis file laporan: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return view;
    }
}