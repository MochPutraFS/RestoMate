package com.restomate.controllers;

import com.restomate.dao.TransactionDAO;
import com.restomate.models.Transaction;
import com.restomate.views.ReportView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ReportController {
    private ReportView view;
    private TransactionDAO transactionDAO;
    private ObservableList<Transaction> txList;
    
    // Variabel state lokal (shift)
    private double currentIncome = 0;
    private double currentCash = 0;
    private double currentQris = 0;
    private int currentTxCount = 0;
    private double currentAov = 0;
    
    private boolean isShiftClosed = false;
    private final NumberFormat numFormatter = NumberFormat.getInstance(new Locale("id", "ID"));

    public ReportController(ReportView view) {
        this.view = view;
        this.transactionDAO = new TransactionDAO();
        this.txList = FXCollections.observableArrayList();
        
        setupTable();
        initDatePickers();
        loadData();
        setupActions();
    }

    public void refresh() {
        loadData();
    }

    private void initDatePickers() {
        LocalDate today = LocalDate.now();
        view.getDpMulai().setValue(today);
        view.getDpSelesai().setValue(today);
    }

    private void setupTable() {
        TableView<Transaction> table = view.getTableTx();
        
        TableColumn<Transaction, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colId.setPrefWidth(50);
        
        TableColumn<Transaction, String> colWaktu = new TableColumn<>("Waktu/Tanggal");
        colWaktu.setCellValueFactory(data -> {
            LocalDateTime dt = data.getValue().getCreatedAt();
            if (dt != null) {
                return new SimpleStringProperty(dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            return new SimpleStringProperty("-");
        });
        colWaktu.setPrefWidth(150);
        
        TableColumn<Transaction, String> colPelanggan = new TableColumn<>("Pelanggan");
        colPelanggan.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaPelanggan() != null ? data.getValue().getNamaPelanggan() : "-"));
        colPelanggan.setPrefWidth(120);
        
        TableColumn<Transaction, String> colAntrian = new TableColumn<>("Antrian");
        colAntrian.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomorAntrian() != null ? data.getValue().getNomorAntrian() : "-"));
        colAntrian.setPrefWidth(80);
        
        TableColumn<Transaction, String> colTipe = new TableColumn<>("Tipe");
        colTipe.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipePesanan() != null ? data.getValue().getTipePesanan() : "-"));
        colTipe.setPrefWidth(120);
        
        TableColumn<Transaction, String> colMetode = new TableColumn<>("Metode");
        colMetode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMetodePembayaran()));
        colMetode.setPrefWidth(90);
        
        TableColumn<Transaction, String> colCatatan = new TableColumn<>("Catatan");
        colCatatan.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCatatan() != null ? data.getValue().getCatatan() : "-"));
        colCatatan.setPrefWidth(160);
        
        TableColumn<Transaction, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(data -> new SimpleStringProperty("Rp " + numFormatter.format(data.getValue().getTotal())));
        colTotal.setPrefWidth(120);
        
        table.getColumns().addAll(colId, colWaktu, colPelanggan, colAntrian, colTipe, colMetode, colCatatan, colTotal);
        table.setItems(txList);
    }

    private void loadData() {
        LocalDate start = view.getDpMulai().getValue();
        LocalDate end = view.getDpSelesai().getValue();
        
        if (start == null) start = LocalDate.now();
        if (end == null) end = LocalDate.now();
        
        // Proteksi: Tombol Tutup Kasir hanya aktif untuk HARI INI saja dan jika shift belum ditutup
        LocalDate today = LocalDate.now();
        boolean isTodayOnly = start.equals(today) && end.equals(today);
        view.getBtnReset().setDisable(!isTodayOnly || isShiftClosed);
        
        if (isTodayOnly && isShiftClosed) {
            currentIncome = 0;
            currentCash = 0;
            currentQris = 0;
            currentTxCount = 0;
            currentAov = 0;
            txList.clear();
        } else {
            List<Transaction> rangeTx = transactionDAO.getTransactionsRange(start.toString(), end.toString());
            txList.setAll(rangeTx);
            
            currentIncome = 0;
            currentCash = 0;
            currentQris = 0;
            currentTxCount = rangeTx.size();
            
            for (Transaction t : rangeTx) {
                currentIncome += t.getTotal();
                if ("CASH".equalsIgnoreCase(t.getMetodePembayaran())) {
                    currentCash += t.getTotal();
                } else if ("QRIS".equalsIgnoreCase(t.getMetodePembayaran())) {
                    currentQris += t.getTotal();
                }
            }
            currentAov = currentTxCount == 0 ? 0 : currentIncome / currentTxCount;
        }
        
        updateUI();
        renderCharts();
    }

    private void updateUI() {
        view.getLblIncome().setText("Rp " + numFormatter.format(currentIncome));
        view.getLblCashIncome().setText("Rp " + numFormatter.format(currentCash));
        view.getLblQrisIncome().setText("Rp " + numFormatter.format(currentQris));
        view.getLblTxCount().setText(String.valueOf(currentTxCount));
        view.getLblAov().setText("Rp " + numFormatter.format(currentAov));
    }

    private void renderCharts() {
        // 1. Render BarChart: Tren Pendapatan Harian
        view.getBarChart().getData().clear();
        if (!txList.isEmpty()) {
            Map<String, Double> dailyMap = new TreeMap<>();
            for (Transaction t : txList) {
                if (t.getCreatedAt() != null) {
                    String dateStr = t.getCreatedAt().toLocalDate().toString();
                    dailyMap.put(dateStr, dailyMap.getOrDefault(dateStr, 0.0) + t.getTotal());
                }
            }
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (Map.Entry<String, Double> entry : dailyMap.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            view.getBarChart().getData().add(series);
        }
        
        // 2. Render PieChart: Proporsi Metode Pembayaran
        view.getPieChart().getData().clear();
        if (currentIncome > 0) {
            PieChart.Data cashSlice = new PieChart.Data("💵 Tunai (Rp " + numFormatter.format(currentCash) + ")", currentCash);
            PieChart.Data qrisSlice = new PieChart.Data("📱 QRIS (Rp " + numFormatter.format(currentQris) + ")", currentQris);
            view.getPieChart().getData().addAll(cashSlice, qrisSlice);
        }
    }

    private void setupActions() {
        // Tombol Filter Tanggal
        view.getBtnFilter().setOnAction(e -> {
            LocalDate start = view.getDpMulai().getValue();
            LocalDate end = view.getDpSelesai().getValue();
            
            if (start == null || end == null) {
                showAlert(Alert.AlertType.WARNING, "Isian Belum Lengkap", "Pilih tanggal mulai dan tanggal selesai terlebih dahulu!");
                return;
            }
            
            if (start.isAfter(end)) {
                showAlert(Alert.AlertType.WARNING, "Rentang Salah", "Tanggal mulai tidak boleh melebihi tanggal selesai!");
                return;
            }
            
            loadData();
        });

        // Tombol Cetak Laporan
        view.getBtnCetak().setOnAction(e -> {
            if (txList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Data Kosong", "Tidak ada data transaksi yang dapat dicetak.");
                return;
            }
            
            LocalDate start = view.getDpMulai().getValue();
            LocalDate end = view.getDpSelesai().getValue();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileName = "Laporan_RestoMate_Rentang_" + timestamp + ".txt";
            
            java.io.File reportsDir = new java.io.File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }
            java.io.File reportFile = new java.io.File(reportsDir, fileName);
            
            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write("=========================================\n");
                writer.write("        LAPORAN PENDAPATAN HARIAN        \n");
                writer.write("               RESTOMATE                 \n");
                writer.write("=========================================\n");
                writer.write("Tanggal Cetak  : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                writer.write("Rentang Laporan: " + start + " s.d " + end + "\n");
                writer.write("Status Shift   : " + (isShiftClosed && start.equals(LocalDate.now()) && end.equals(LocalDate.now()) ? "DITUTUP" : "AKTIF") + "\n");
                writer.write("-----------------------------------------\n");
                writer.write("TOTAL OMSET    : Rp " + numFormatter.format(currentIncome) + "\n");
                writer.write("  - TUNAI/CASH : Rp " + numFormatter.format(currentCash) + "\n");
                writer.write("  - QRIS       : Rp " + numFormatter.format(currentQris) + "\n");
                writer.write("-----------------------------------------\n");
                writer.write("Jml Transaksi  : " + currentTxCount + " struk\n");
                writer.write("Rata-rata (AOV): Rp " + numFormatter.format(currentAov) + "\n");
                writer.write("=========================================================================================\n");
                writer.write("                                     DAFTAR TRANSAKSI                                    \n");
                writer.write("=========================================================================================\n");
                writer.write(String.format("%-4s | %-19s | %-15s | %-8s | %-15s | %-6s | %-12s\n", "ID", "Waktu/Tanggal", "Pelanggan", "Antrian", "Tipe", "Metode", "Total"));
                writer.write("-----------------------------------------------------------------------------------------\n");
                for (Transaction t : txList) {
                    String time = t.getCreatedAt() != null ? t.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "-";
                    String pelanggan = t.getNamaPelanggan() != null ? t.getNamaPelanggan() : "-";
                    if (pelanggan.length() > 15) pelanggan = pelanggan.substring(0, 12) + "...";
                    String antrian = t.getNomorAntrian() != null ? t.getNomorAntrian() : "-";
                    if (antrian.length() > 8) antrian = antrian.substring(0, 5) + "...";
                    String tipe = t.getTipePesanan() != null ? t.getTipePesanan() : "-";
                    if (tipe.length() > 15) tipe = tipe.substring(0, 12) + "...";
                    writer.write(String.format("%-4d | %-19s | %-15s | %-8s | %-15s | %-6s | Rp %s\n", 
                        t.getId(), time, pelanggan, antrian, tipe, t.getMetodePembayaran(), numFormatter.format(t.getTotal())));
                }
                writer.write("=========================================================================================\n");
                writer.write("Terima kasih atas kerja keras hari ini!\n");
                
                showAlert(Alert.AlertType.INFORMATION, "Ekspor Sukses", "File laporan tercetak di: " + reportFile.getPath());
            } catch (IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Gagal Cetak", "Gagal menulis file laporan: " + ex.getMessage());
            }
        });

        // Tombol Tutup Kasir (Shift Reset)
        view.getBtnReset().setOnAction(e -> {
            if (isShiftClosed) {
                showAlert(Alert.AlertType.INFORMATION, "Kasir Sudah Ditutup", "Shift kasir ini sudah diselesaikan sebelumnya.");
                return;
            }
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Tutup Kasir Shift");
            confirm.setHeaderText("Konfirmasi Penutupan Shift");
            confirm.setContentText("Apakah Anda yakin ingin menyelesaikan shift ini? Seluruh ringkasan statistik dan histori audit hari ini di layar akan direset ke nol. Data database tetap tersimpan dengan aman.");
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    isShiftClosed = true;
                    loadData();
                    showAlert(Alert.AlertType.INFORMATION, "Shift Berakhir", "Shift kasir resmi ditutup. Angka pendapatan dan audit direset untuk shift berikutnya.");
                }
            });
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
