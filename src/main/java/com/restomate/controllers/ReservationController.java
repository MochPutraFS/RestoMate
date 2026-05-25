package com.restomate.controllers;

import com.restomate.dao.ReservationDAO;
import com.restomate.dao.TableDAO;
import com.restomate.models.Reservation;
import com.restomate.views.ReservationView;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.effect.DropShadow;
import javafx.geometry.Insets;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class ReservationController {
    private ReservationView view;
    private ReservationDAO dao;
    private TableDAO tableDAO;
    private boolean isPolling = true; // Flag for background thread
    private Thread pollingThread;

    public ReservationController(ReservationView view) {
        this.view = view;
        this.dao = new ReservationDAO();
        this.tableDAO = new TableDAO();
        
        setupActions();
        refreshVisuals();
        startPolling();
    }
    
    private void setupActions() {
        // Hanya izinkan angka di input kelola meja
        view.getTxtCustomMeja().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                view.getTxtCustomMeja().setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // Booking Meja
        view.getBtnSimpan().setOnAction(e -> {
            try {
                String nama = view.getTxtNama().getText().trim();
                String mejaText = view.getTxtMeja().getText().trim();
                LocalDate tgl = view.getDpTanggal().getValue();
                String jam = view.getCmbJam().getValue() + ":" + view.getCmbMenit().getValue();
                
                if (nama.isEmpty() || mejaText.isEmpty() || tgl == null) {
                    showAlert(Alert.AlertType.WARNING, "Isian Belum Lengkap", "Nama pelanggan dan pilihan meja wajib diisi!");
                    return;
                }
                
                int noMeja = Integer.parseInt(mejaText);
                LocalTime time = LocalTime.parse(jam, DateTimeFormatter.ofPattern("HH:mm"));
                LocalDateTime datetime = LocalDateTime.of(tgl, time);
                
                Reservation r = new Reservation(0, nama, noMeja, datetime, "AKTIF");
                if (dao.addReservation(r)) {
                    showAlert(Alert.AlertType.INFORMATION, "Pemesanan Sukses", "Meja " + noMeja + " berhasil di-booking atas nama " + nama + "!");
                    
                    // Reset Form
                    view.getTxtNama().clear();
                    view.getTxtMeja().clear();
                    view.getDpTanggal().setValue(null);
                    
                    // Segarkan Visual secara Instan
                    refreshVisuals();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal Booking", "Terjadi kegagalan saat menyimpan data reservasi.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Nomor Meja Salah", "Nomor meja harus berupa angka!");
            } catch (DateTimeParseException ex) {
                showAlert(Alert.AlertType.WARNING, "Format Waktu Salah", "Pilihan waktu tidak valid.");
            }
        });

        // Tambah Meja Kustom
        view.getBtnTambahMeja().setOnAction(e -> {
            String inputMeja = view.getTxtCustomMeja().getText().trim();
            if (inputMeja.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Kosong", "Masukkan nomor meja yang ingin ditambahkan!");
                return;
            }

            try {
                int nomorMeja = Integer.parseInt(inputMeja);
                if (nomorMeja <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Nomor Meja Tidak Valid", "Nomor meja harus lebih besar dari 0!");
                    return;
                }

                if (tableDAO.addTable(nomorMeja)) {
                    showAlert(Alert.AlertType.INFORMATION, "Meja Ditambahkan", "Meja baru nomor " + nomorMeja + " berhasil ditambahkan.");
                    view.getTxtCustomMeja().clear();
                    refreshVisuals();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Gagal Menambahkan Meja", "Meja nomor " + nomorMeja + " sudah terdaftar!");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Format Salah", "Nomor meja harus berupa angka bulat!");
            }
        });

        // Hapus Meja Kustom
        view.getBtnHapusMeja().setOnAction(e -> {
            String inputMeja = view.getTxtCustomMeja().getText().trim();
            if (inputMeja.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Kosong", "Masukkan nomor meja yang ingin dihapus!");
                return;
            }

            try {
                int nomorMeja = Integer.parseInt(inputMeja);
                List<Integer> allTables = tableDAO.getAllTableNumbers();
                if (!allTables.contains(nomorMeja)) {
                    showAlert(Alert.AlertType.WARNING, "Tidak Ditemukan", "Meja nomor " + nomorMeja + " tidak ada dalam daftar.");
                    return;
                }

                // Cek apakah ada reservasi aktif pada meja ini
                if (dao.hasActiveReservation(nomorMeja)) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Konfirmasi Hapus Meja");
                    confirm.setHeaderText("Reservasi Aktif Ditemukan");
                    confirm.setContentText("Meja nomor " + nomorMeja + " memiliki reservasi aktif. Menghapus meja ini akan membatalkan semua reservasi aktif tersebut secara otomatis. Lanjutkan?");
                    
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        dao.cancelReservationsForTable(nomorMeja);
                        tableDAO.deleteTable(nomorMeja);
                        showAlert(Alert.AlertType.INFORMATION, "Meja & Reservasi Dihapus", "Meja " + nomorMeja + " dan seluruh reservasi aktifnya berhasil dihapus.");
                        view.getTxtCustomMeja().clear();
                        refreshVisuals();
                    }
                } else {
                    if (tableDAO.deleteTable(nomorMeja)) {
                        showAlert(Alert.AlertType.INFORMATION, "Meja Dihapus", "Meja nomor " + nomorMeja + " berhasil dihapus.");
                        view.getTxtCustomMeja().clear();
                        refreshVisuals();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus meja.");
                    }
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Format Salah", "Nomor meja harus berupa angka!");
            }
        });
    }

    private void refreshVisuals() {
        List<Reservation> allRes = dao.getAllReservations();
        updateTableVisuals(allRes);
    }

    // Helper buat bikin bentuk meja
    private VBox createTableBox(int nomor, String status, String namaPelanggan, int reservationId) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(100, 100);
        
        // Warna dinamis: Ijo untuk kosong, merah untuk dipesan
        Color bgColor = "AKTIF".equals(status) ? Color.web("#FFCDD2") : Color.web("#C8E6C9");
        box.setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(10), Insets.EMPTY)));
        
        // Shadow modern
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.05));
        shadow.setRadius(5);
        box.setEffect(shadow);

        Label lblNo = new Label("Meja " + nomor);
        lblNo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblNo.setTextFill(Color.web("#333333"));
        
        Label lblStatus = new Label("AKTIF".equals(status) ? "Dipesan" : "Tersedia");
        lblStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblStatus.setTextFill("AKTIF".equals(status) ? Color.web("#C62828") : Color.web("#2E7D32"));
        
        Label lblNama = new Label("AKTIF".equals(status) ? namaPelanggan : "");
        lblNama.setFont(Font.font("Segoe UI", 11));
        lblNama.setTextFill(Color.web("#555555"));
        
        box.getChildren().addAll(lblNo, lblStatus, lblNama);

        // Kursor tangan
        box.setOnMouseEntered(e -> box.setCursor(javafx.scene.Cursor.HAND));

        // Tooltip
        if ("AKTIF".equals(status)) {
            Tooltip tooltip = new Tooltip("Meja " + nomor + " - Dipesan oleh: " + namaPelanggan + "\nKlik untuk menyelesaikan/membatalkan booking.");
            Tooltip.install(box, tooltip);
        } else {
            Tooltip tooltip = new Tooltip("Meja " + nomor + " Tersedia\nKlik untuk memilih meja ini.");
            Tooltip.install(box, tooltip);
        }

        // Aksi klik meja
        box.setOnMouseClicked(e -> {
            if ("AKTIF".equals(status)) {
                showManageReservationDialog(nomor, namaPelanggan, reservationId);
            } else {
                view.getTxtMeja().setText(String.valueOf(nomor));
            }
        });

        return box;
    }

    private void showManageReservationDialog(int nomorMeja, String namaPelanggan, int reservationId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kelola Reservasi - Meja " + nomorMeja);
        alert.setHeaderText("Reservasi Meja " + nomorMeja + " oleh: " + namaPelanggan);
        alert.setContentText("Silakan tentukan tindakan selanjutnya:");

        ButtonType btnSelesai = new ButtonType("Selesaikan");
        ButtonType btnBatalBooking = new ButtonType("Batalkan Booking");
        ButtonType btnClose = new ButtonType("Kembali", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnSelesai, btnBatalBooking, btnClose);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnSelesai) {
                if (dao.updateStatus(reservationId, "SELESAI")) {
                    showAlert(Alert.AlertType.INFORMATION, "Reservasi Selesai", "Meja nomor " + nomorMeja + " kini kosong.");
                    refreshVisuals();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui status reservasi.");
                }
            } else if (result.get() == btnBatalBooking) {
                if (dao.updateStatus(reservationId, "BATAL")) {
                    showAlert(Alert.AlertType.INFORMATION, "Reservasi Dibatalkan", "Reservasi meja nomor " + nomorMeja + " telah dibatalkan.");
                    refreshVisuals();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal membatalkan reservasi.");
                }
            }
        }
    }

    // Polling Latar Belakang (Sinkronisasi Live)
    private void startPolling() {
        pollingThread = new Thread(() -> {
            while (isPolling) {
                try {
                    List<Reservation> allRes = dao.getAllReservations();
                    Platform.runLater(() -> {
                        updateTableVisuals(allRes);
                    });
                    
                    Thread.sleep(5000); // Poll every 5 seconds
                } catch (InterruptedException e) {
                    System.out.println("Polling thread dihentikan.");
                    break;
                }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private void updateTableVisuals(List<Reservation> reservations) {
        view.getTableGrid().getChildren().clear();
        int col = 0, row = 0;

        List<Integer> activeTables = tableDAO.getAllTableNumbers();
        for (int tableNum : activeTables) {
            String status = "KOSONG";
            String pelanggan = "";
            int resId = 0;

            for (Reservation r : reservations) {
                if (r.getNomorMeja() == tableNum && "AKTIF".equals(r.getStatus())) {
                    status = "AKTIF";
                    pelanggan = r.getNamaPelanggan();
                    resId = r.getId();
                    break;
                }
            }

            VBox box = createTableBox(tableNum, status, pelanggan, resId);
            view.getTableGrid().add(box, col, row);

            col++;
            if (col == 4) { // 4 kolom per baris
                col = 0;
                row++;
            }
        }
    }

    public void stopPolling() {
        isPolling = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
