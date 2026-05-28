package com.restomate.controllers;

import com.restomate.dao.ReservationDAO;
import com.restomate.dao.TableDAO;
import com.restomate.models.Reservation;
import com.restomate.models.RestaurantTable;
import com.restomate.views.ReservationView;
import com.restomate.views.DashboardView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationController {
    private ReservationView view;
    private ReservationDAO dao;
    private TableDAO tableDAO;
    private boolean isPolling = true; // Flag for background thread
    private Thread pollingThread;
    
    private List<RestaurantTable> allTablesList;

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

        // Hanya izinkan angka di input kapasitas meja
        view.getTxtCustomKapasitas().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                view.getTxtCustomKapasitas().setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // Tambah Meja Kustom
        view.getBtnTambahMeja().setOnAction(e -> {
            String inputMeja = view.getTxtCustomMeja().getText().trim();
            String inputKapasitas = view.getTxtCustomKapasitas().getText().trim();
            
            if (inputMeja.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Kosong", "Masukkan nomor meja yang ingin ditambahkan!");
                return;
            }

            int kapasitas = 4; // default
            if (!inputKapasitas.isEmpty()) {
                try {
                    kapasitas = Integer.parseInt(inputKapasitas);
                    if (kapasitas <= 0) {
                        showAlert(Alert.AlertType.WARNING, "Kapasitas Tidak Valid", "Kapasitas meja harus lebih besar dari 0!");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.WARNING, "Format Salah", "Kapasitas meja harus berupa angka bulat!");
                    return;
                }
            }

            try {
                int nomorMeja = Integer.parseInt(inputMeja);
                if (nomorMeja <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Nomor Meja Tidak Valid", "Nomor meja harus lebih besar dari 0!");
                    return;
                }

                if (tableDAO.addTable(nomorMeja, kapasitas)) {
                    showAlert(Alert.AlertType.INFORMATION, "Meja Ditambahkan", "Meja baru nomor " + nomorMeja + " dengan kapasitas " + kapasitas + " orang berhasil ditambahkan.");
                    view.getTxtCustomMeja().clear();
                    view.getTxtCustomKapasitas().clear();
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

        // Live Status & Filter Waktu Listeners
        view.getChkLiveStatus().selectedProperty().addListener((obs, oldVal, newVal) -> {
            refreshVisuals();
        });
        view.getDpFilterTanggal().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!view.getChkLiveStatus().isSelected()) {
                refreshVisuals();
            }
        });
        view.getCmbFilterJam().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!view.getChkLiveStatus().isSelected()) {
                refreshVisuals();
            }
        });
        view.getCmbFilterMenit().valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!view.getChkLiveStatus().isSelected()) {
                refreshVisuals();
            }
        });

        // Search Daftar Booking
        view.getTxtSearchDaftar().textProperty().addListener((obs, oldVal, newVal) -> {
            refreshVisuals();
        });

        // Double click row in TableView Daftar Booking
        view.getTblReservasi().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Reservation selected = view.getTblReservasi().getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showManageReservationDialog(selected);
                }
            }
        });
    }

    private static class PreOrderMenuRow {
        com.restomate.models.MenuRestoran menu;
        CheckBox chkSelect;
        Spinner<Integer> spnQty;
        Label lblSubtotal;
        HBox container;

        PreOrderMenuRow(com.restomate.models.MenuRestoran menu) {
            this.menu = menu;
            
            chkSelect = new CheckBox(menu.getNama());
            chkSelect.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            chkSelect.setTextFill(Color.web("#2C1A0E"));
            
            Label lblStockPrice = new Label(String.format(" (Stok: %d | Rp%,.0f)", menu.getStok(), menu.getHarga()));
            lblStockPrice.setFont(Font.font("Segoe UI", 9));
            lblStockPrice.setTextFill(Color.web("#7F8C8D"));
            
            HBox leftSide = new HBox(3, chkSelect, lblStockPrice);
            leftSide.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(leftSide, Priority.ALWAYS);
            
            spnQty = new Spinner<>(1, Math.max(1, menu.getStok()), 1);
            spnQty.setPrefWidth(75);
            spnQty.setDisable(true);
            spnQty.setStyle("-fx-font-size: 10px;");
            
            lblSubtotal = new Label("Rp 0");
            lblSubtotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            lblSubtotal.setTextFill(Color.web("#C0392B"));
            lblSubtotal.setPrefWidth(85);
            lblSubtotal.setAlignment(Pos.CENTER_RIGHT);
            
            container = new HBox(8, leftSide, spnQty, lblSubtotal);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(4, 5, 4, 5));
            container.setStyle("-fx-border-color: #E8DDD0; -fx-border-width: 0 0 1 0;");
            
            // Listeners
            chkSelect.selectedProperty().addListener((obs, oldVal, newVal) -> {
                spnQty.setDisable(!newVal);
                updateSubtotal();
            });
            spnQty.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateSubtotal();
            });
        }
        
        void updateSubtotal() {
            double sub = chkSelect.isSelected() ? (menu.getHarga() * spnQty.getValue()) : 0.0;
            java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
            lblSubtotal.setText("Rp " + formatter.format(sub));
        }
        
        double getSubtotalValue() {
            return chkSelect.isSelected() ? (menu.getHarga() * spnQty.getValue()) : 0.0;
        }
    }

    private void showBookingDialog(int nomorMeja, int kapasitas) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Catat Reservasi - Meja " + nomorMeja);
        dialog.setHeaderText("Booking Meja " + nomorMeja + " (Kapasitas: " + kapasitas + " Orang)");

        // Set buttons
        ButtonType btnSimpanType = new ButtonType("Booking Meja", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnBatalType = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSimpanType, btnBatalType);

        // Styling dialog pane
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FDF6EC; -fx-padding: 10px;");
        dialogPane.setPrefWidth(380);

        String fieldStyle = "-fx-font-size: 12px; -fx-background-radius: 7; -fx-border-radius: 7; -fx-border-color: #E8DDD0; -fx-padding: 7;";

        TextField txtNama = new TextField();
        txtNama.setPromptText("Nama pelanggan...");
        txtNama.setStyle(fieldStyle);
        txtNama.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> spnJumlahOrang = new Spinner<>(1, 100, 2);
        spnJumlahOrang.setEditable(true);
        spnJumlahOrang.setMaxWidth(Double.MAX_VALUE);
        spnJumlahOrang.setStyle("-fx-font-size: 12px;");

        DatePicker dpTanggal = new DatePicker(LocalDate.now());
        dpTanggal.setPromptText("Pilih Tanggal");
        dpTanggal.setMaxWidth(Double.MAX_VALUE);
        dpTanggal.setStyle("-fx-font-size: 12px;");

        HBox timeBox = new HBox(8);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cmbJam = new ComboBox<>();
        for (int h = 8; h <= 22; h++) cmbJam.getItems().add(String.format("%02d", h));
        cmbJam.setValue("12");
        cmbJam.setStyle("-fx-font-size: 12px; -fx-pref-width: 68px;");

        Label lblSep = new Label(":");
        lblSep.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblSep.setTextFill(Color.web("#2C1A0E"));

        ComboBox<String> cmbMenit = new ComboBox<>();
        cmbMenit.getItems().addAll("00", "15", "30", "45");
        cmbMenit.setValue("00");
        cmbMenit.setStyle("-fx-font-size: 12px; -fx-pref-width: 68px;");

        timeBox.getChildren().addAll(cmbJam, lblSep, cmbMenit);

        TextField txtCatatan = new TextField();
        txtCatatan.setPromptText("Pesan khusus (mis: baby chair)...");
        txtCatatan.setStyle(fieldStyle);
        txtCatatan.setMaxWidth(Double.MAX_VALUE);

        CheckBox chkPreOrder = new CheckBox("Pre-order Menu Makanan");
        chkPreOrder.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        chkPreOrder.setTextFill(Color.web("#2C1A0E"));

        VBox preOrderBox = new VBox(8);
        preOrderBox.setPadding(new Insets(5, 0, 5, 0));
        preOrderBox.setVisible(false);
        preOrderBox.setManaged(false);

        chkPreOrder.selectedProperty().addListener((obs, oldVal, newVal) -> {
            preOrderBox.setVisible(newVal);
            preOrderBox.setManaged(newVal);
            dialogPane.getScene().getWindow().sizeToScene(); // Resize dialog dynamically
        });

        // Upgraded Pre-order menu filter & list
        TextField txtCariMenu = new TextField();
        txtCariMenu.setPromptText("Cari menu pre-order...");
        txtCariMenu.setStyle(fieldStyle);
        txtCariMenu.setMaxWidth(Double.MAX_VALUE);
        
        VBox vboxMenuList = new VBox(2);
        vboxMenuList.setStyle("-fx-background-color: white;");
        
        List<PreOrderMenuRow> menuRows = new ArrayList<>();
        List<com.restomate.models.MenuRestoran> dbMenus = new com.restomate.dao.MenuDAO().getAllMenus();
        for (com.restomate.models.MenuRestoran m : dbMenus) {
            if (m.getStok() > 0) { // Hanya tampilkan menu yang stoknya > 0
                PreOrderMenuRow row = new PreOrderMenuRow(m);
                menuRows.add(row);
                vboxMenuList.getChildren().add(row.container);
            }
        }
        
        ScrollPane scrollMenuList = new ScrollPane(vboxMenuList);
        scrollMenuList.setFitToWidth(true);
        scrollMenuList.setPrefHeight(160);
        scrollMenuList.setStyle("-fx-background-color: white; -fx-border-color: #E8DDD0; -fx-border-radius: 7; -fx-background-radius: 7;");
        
        Label lblTotalEstimation = new Label("Estimasi Total Pre-Order: Rp 0");
        lblTotalEstimation.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblTotalEstimation.setTextFill(Color.web("#C0392B"));
        
        // Filter menu list dynamically
        txtCariMenu.textProperty().addListener((obs, oldVal, newVal) -> {
            String query = newVal.trim().toLowerCase();
            vboxMenuList.getChildren().clear();
            for (PreOrderMenuRow row : menuRows) {
                if (query.isEmpty() || row.menu.getNama().toLowerCase().contains(query)) {
                    vboxMenuList.getChildren().add(row.container);
                }
            }
        });
        
        // Real-time total estimation update helper
        Runnable updateGrandTotal = () -> {
            double total = 0;
            for (PreOrderMenuRow row : menuRows) {
                total += row.getSubtotalValue();
            }
            java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
            lblTotalEstimation.setText("Estimasi Total Pre-Order: Rp " + formatter.format(total));
        };
        
        // Add listeners to all rows to trigger grand total updates
        for (PreOrderMenuRow row : menuRows) {
            row.chkSelect.selectedProperty().addListener((obs, oldVal, newVal) -> updateGrandTotal.run());
            row.spnQty.valueProperty().addListener((obs, oldVal, newVal) -> updateGrandTotal.run());
        }

        HBox readyTimeBox = new HBox(8);
        readyTimeBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cmbReadyJam = new ComboBox<>();
        for (int h = 8; h <= 22; h++) cmbReadyJam.getItems().add(String.format("%02d", h));
        cmbReadyJam.setValue("12");
        cmbReadyJam.setStyle("-fx-font-size: 12px; -fx-pref-width: 68px;");

        Label lblReadySep = new Label(":");
        lblReadySep.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblReadySep.setTextFill(Color.web("#2C1A0E"));

        ComboBox<String> cmbReadyMenit = new ComboBox<>();
        cmbReadyMenit.getItems().addAll("00", "15", "30", "45");
        cmbReadyMenit.setValue("00");
        cmbReadyMenit.setStyle("-fx-font-size: 12px; -fx-pref-width: 68px;");

        readyTimeBox.getChildren().addAll(cmbReadyJam, lblReadySep, cmbReadyMenit);

        preOrderBox.getChildren().addAll(
                makeLabel("Cari Menu Makanan:"),
                txtCariMenu,
                scrollMenuList,
                lblTotalEstimation,
                makeLabel("Waktu Makanan Siap (Ready Time):"),
                readyTimeBox
        );

        VBox content = new VBox(10);
        content.getChildren().addAll(
                makeLabel("Nama Pelanggan:"), txtNama,
                makeLabel("Jumlah Orang:"), spnJumlahOrang,
                makeLabel("Tanggal:"), dpTanggal,
                makeLabel("Waktu Reservasi:"), timeBox,
                makeLabel("Catatan (Request Khusus):"), txtCatatan,
                chkPreOrder,
                preOrderBox
        );
        dialogPane.setContent(content);

        // Validation & Action filter on "Booking Meja"
        Button btnSimpan = (Button) dialogPane.lookupButton(btnSimpanType);
        btnSimpan.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                String nama = txtNama.getText().trim();
                LocalDate tgl = dpTanggal.getValue();
                String jam = cmbJam.getValue() + ":" + cmbMenit.getValue();
                
                if (nama.isEmpty() || tgl == null) {
                    showAlert(Alert.AlertType.WARNING, "Isian Belum Lengkap", "Nama pelanggan dan tanggal reservasi wajib diisi!");
                    event.consume(); // prevent closing
                    return;
                }
                
                int jumlahOrang = spnJumlahOrang.getValue();
                
                // Cek kapasitas meja
                if (jumlahOrang > kapasitas) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Kapasitas Meja Terlampaui");
                    confirm.setHeaderText("Jumlah Orang Melebihi Kapasitas Meja");
                    confirm.setContentText("Meja nomor " + nomorMeja + " memiliki kapasitas " + kapasitas + 
                            " orang, sedangkan Anda memesan untuk " + jumlahOrang + " orang. Tetap lanjutkan?");
                    
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        event.consume(); // prevent closing
                        return;
                    }
                }

                LocalTime time = LocalTime.parse(jam, DateTimeFormatter.ofPattern("HH:mm"));
                LocalDateTime datetime = LocalDateTime.of(tgl, time);
                
                // Cek bentrok jadwal (Double-booking conflict validation)
                Reservation conflict = dao.getConflictingReservation(nomorMeja, datetime);
                if (conflict != null) {
                    showAlert(Alert.AlertType.WARNING, "Bentrok Jadwal Reservasi", 
                            "Pemesanan dibatalkan karena bentrok dengan jadwal " + conflict.getNamaPelanggan() + 
                            " pada pukul " + conflict.getWaktuReservasi().format(DateTimeFormatter.ofPattern("HH:mm")) + 
                            " (selisih < 2 jam).");
                    event.consume();
                    return;
                }

                String menuDipesan = "";
                String waktuSiap = "";
                if (chkPreOrder.isSelected()) {
                    StringBuilder sbMenu = new StringBuilder();
                    for (PreOrderMenuRow row : menuRows) {
                        if (row.chkSelect.isSelected()) {
                            if (sbMenu.length() > 0) sbMenu.append(", ");
                            sbMenu.append(row.menu.getNama()).append(" x").append(row.spnQty.getValue());
                        }
                    }
                    menuDipesan = sbMenu.toString();
                    waktuSiap = cmbReadyJam.getValue() + ":" + cmbReadyMenit.getValue();
                }
                String catatan = txtCatatan.getText().trim();
                
                Reservation r = new Reservation(0, nama, nomorMeja, datetime, "AKTIF", jumlahOrang, menuDipesan, catatan, waktuSiap);
                if (dao.addReservation(r)) {
                    showAlert(Alert.AlertType.INFORMATION, "Pemesanan Sukses", "Meja " + nomorMeja + " berhasil di-booking atas nama " + nama + "!");
                    refreshVisuals();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal Booking", "Terjadi kegagalan saat menyimpan data reservasi.");
                    event.consume();
                }
            } catch (DateTimeParseException ex) {
                showAlert(Alert.AlertType.WARNING, "Format Waktu Salah", "Pilihan waktu tidak valid.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private Label makeLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web("#555555"));
        return lbl;
    }

    private void refreshVisuals() {
        List<Reservation> allRes = dao.getAllReservations();
        updateTableVisuals(allRes);
    }

    private RestaurantTable findTableByNumber(int tableNum) {
        if (allTablesList != null) {
            for (RestaurantTable t : allTablesList) {
                if (t.getNomorMeja() == tableNum) {
                    return t;
                }
            }
        }
        return null;
    }

    // Helper buat bikin bentuk meja premium tanpa titik-titik kursi
    private Pane createTableBox(int nomor, String status, Reservation r, int kapasitas) {
        VBox tableBody = new VBox(3);
        tableBody.setAlignment(Pos.CENTER);
        
        // Ukuran dan radius meja dinamis
        double width = 86;
        double height = 86;
        String shapeStyle = "";
        
        if (kapasitas <= 3) {
            // Meja Bulat
            width = 86;
            height = 86;
            shapeStyle = "-fx-background-radius: 50; -fx-border-radius: 50;";
        } else if (kapasitas == 4) {
            // Meja Kotak
            width = 86;
            height = 86;
            shapeStyle = "-fx-background-radius: 10; -fx-border-radius: 10;";
        } else {
            // Meja Panjang (Kapasitas > 4)
            width = 110;
            height = 86;
            shapeStyle = "-fx-background-radius: 10; -fx-border-radius: 10;";
        }
        
        tableBody.setPrefSize(width, height);
        tableBody.setMinSize(width, height);
        tableBody.setMaxSize(width, height);
        
        // Warna dinamis: Ijo untuk kosong, merah untuk dipesan
        String bgColor = "AKTIF".equals(status) ? "#FDEDEC" : "#EAFAF1";
        String borderColor = "AKTIF".equals(status) ? "#F5B7B1" : "#A9DFBF";
        String textFillColor = "AKTIF".equals(status) ? "#C0392B" : "#27AE60";
        
        tableBody.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-border-color: " + borderColor + "; " +
            "-fx-border-width: 1.5; " +
            shapeStyle +
            " -fx-padding: 5;"
        );
        
        // Shadow modern
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.06));
        shadow.setRadius(6);
        tableBody.setEffect(shadow);

        Label lblNo = new Label("Meja " + nomor);
        lblNo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblNo.setTextFill(Color.web("#2C1A0E"));
        
        Label lblStatus = new Label("AKTIF".equals(status) ? "Dipesan" : "Tersedia");
        lblStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        lblStatus.setTextFill(Color.web(textFillColor));

        Label lblCap = new Label("Kapasitas: " + kapasitas);
        lblCap.setFont(Font.font("Segoe UI", 8));
        lblCap.setTextFill(Color.web("#7F8C8D"));
        
        Label lblNama = new Label("AKTIF".equals(status) && r != null ? r.getNamaPelanggan() : "");
        lblNama.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        lblNama.setTextFill(Color.web("#34495E"));
        lblNama.setMaxWidth(width - 10);
        lblNama.setAlignment(Pos.CENTER);
        
        tableBody.getChildren().addAll(lblNo, lblStatus, lblCap, lblNama);

        // Container utama untuk membungkus meja
        StackPane container = new StackPane();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));
        container.getChildren().add(tableBody);

        // Animasi mikro hover skala (1.05x)
        ScaleTransition st = new ScaleTransition(Duration.millis(150), container);
        container.setOnMouseEntered(e -> {
            container.setCursor(javafx.scene.Cursor.HAND);
            st.setFromX(container.getScaleX());
            st.setFromY(container.getScaleY());
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        container.setOnMouseExited(e -> {
            st.setFromX(container.getScaleX());
            st.setFromY(container.getScaleY());
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // Tooltip detail
        if ("AKTIF".equals(status) && r != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Meja ").append(nomor).append(" (Kapasitas: ").append(kapasitas).append(" Orang)\n");
            sb.append("Dipesan oleh: ").append(r.getNamaPelanggan()).append("\n");
            sb.append("Jumlah Tamu: ").append(r.getJumlahOrang()).append(" Orang\n");
            
            if (r.getMenuDipesan() != null && !r.getMenuDipesan().trim().isEmpty()) {
                sb.append("Menu Pre-order: ").append(r.getMenuDipesan()).append("\n");
                if (r.getWaktuSiap() != null && !r.getWaktuSiap().trim().isEmpty()) {
                    sb.append("Waktu Siap: ").append(r.getWaktuSiap()).append("\n");
                }
            }
            if (r.getCatatan() != null && !r.getCatatan().trim().isEmpty()) {
                sb.append("Catatan: ").append(r.getCatatan()).append("\n");
            }
            sb.append("Waktu Reservasi: ").append(r.getWaktuReservasi().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))).append("\n");
            sb.append("Klik untuk mengelola booking.");
            
            Tooltip tooltip = new Tooltip(sb.toString());
            tooltip.setFont(Font.font("Segoe UI", 11));
            Tooltip.install(container, tooltip);
        } else {
            Tooltip tooltip = new Tooltip("Meja " + nomor + " (Kapasitas: " + kapasitas + " Orang) Tersedia\nKlik untuk memilih meja ini.");
            tooltip.setFont(Font.font("Segoe UI", 11));
            Tooltip.install(container, tooltip);
        }

        // Aksi klik meja
        container.setOnMouseClicked(e -> {
            if ("AKTIF".equals(status) && r != null) {
                showManageReservationDialog(r);
            } else {
                showBookingDialog(nomor, kapasitas);
            }
        });

        return container;

    }

    private void showManageReservationDialog(Reservation r) {
        StringBuilder details = new StringBuilder();
        details.append("Detail Reservasi:\n");
        details.append("-----------------------------\n");
        details.append("• Waktu Reservasi: ").append(r.getWaktuReservasi().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))).append("\n");
        details.append("• Jumlah Tamu:    ").append(r.getJumlahOrang()).append(" Orang\n");
        
        if (r.getMenuDipesan() != null && !r.getMenuDipesan().trim().isEmpty()) {
            details.append("• Menu Pre-order: ").append(r.getMenuDipesan()).append("\n");
            if (r.getWaktuSiap() != null && !r.getWaktuSiap().trim().isEmpty()) {
                details.append("• Ready Time:     Pukul ").append(r.getWaktuSiap()).append("\n");
            }
        } else {
            details.append("• Menu Pre-order: -\n");
        }
        
        details.append("• Catatan Khusus: ").append((r.getCatatan() != null && !r.getCatatan().trim().isEmpty()) ? r.getCatatan() : "-").append("\n\n");

        // Jika statusnya bukan AKTIF, tampilkan dalam bentuk dialog info read-only
        if (!"AKTIF".equals(r.getStatus())) {
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Detail Reservasi - Meja " + r.getNomorMeja());
            infoAlert.setHeaderText("Reservasi Meja " + r.getNomorMeja() + " oleh: " + r.getNamaPelanggan() + " (" + r.getStatus() + ")");
            infoAlert.setContentText(details.toString());
            infoAlert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kelola Reservasi - Meja " + r.getNomorMeja());
        alert.setHeaderText("Reservasi Meja " + r.getNomorMeja() + " oleh: " + r.getNamaPelanggan());
        
        details.append("Silakan tentukan tindakan selanjutnya:");
        alert.setContentText(details.toString());

        // Buttons for active bookings
        ButtonType btnCheckIn = new ButtonType("Check-In (POS)");
        ButtonType btnSelesai = new ButtonType("Selesaikan");
        ButtonType btnBatalBooking = new ButtonType("Batalkan Booking");
        ButtonType btnClose = new ButtonType("Kembali", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnCheckIn, btnSelesai, btnBatalBooking, btnClose);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnCheckIn) {
                // Check-In POS Action
                if (dao.updateStatus(r.getId(), "SELESAI")) {
                    refreshVisuals();
                    if (DashboardView.getInstance() != null) {
                        DashboardView.getInstance().switchToCashierAndLoadPreOrder(r.getNamaPelanggan(), r.getMenuDipesan());
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Peringatan", "Gagal memindahkan ke Kasir karena dashboard tidak aktif.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal melakukan Check-In.");
                }
            } else if (result.get() == btnSelesai) {
                if (dao.updateStatus(r.getId(), "SELESAI")) {
                    showAlert(Alert.AlertType.INFORMATION, "Reservasi Selesai", "Meja nomor " + r.getNomorMeja() + " kini kosong.");
                    refreshVisuals();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui status reservasi.");
                }
            } else if (result.get() == btnBatalBooking) {
                if (dao.updateStatus(r.getId(), "BATAL")) {
                    showAlert(Alert.AlertType.INFORMATION, "Reservasi Dibatalkan", "Reservasi meja nomor " + r.getNomorMeja() + " telah dibatalkan.");
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

    private LocalDateTime getSelectedFilterTime() {
        if (view.getChkLiveStatus().isSelected()) {
            return LocalDateTime.now();
        }
        LocalDate date = view.getDpFilterTanggal().getValue();
        if (date == null) {
            date = LocalDate.now();
        }
        String jam = view.getCmbFilterJam().getValue();
        String menit = view.getCmbFilterMenit().getValue();
        if (jam == null) jam = "12";
        if (menit == null) menit = "00";
        try {
            return LocalDateTime.of(date, LocalTime.parse(jam + ":" + menit, DateTimeFormatter.ofPattern("HH:mm")));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private void updateTableVisuals(List<Reservation> reservations) {
        view.getTableGrid().getChildren().clear();
        int col = 0, row = 0;
        
        LocalDateTime targetTime = getSelectedFilterTime();

        this.allTablesList = tableDAO.getAllTables();
        for (RestaurantTable t : allTablesList) {
            int tableNum = t.getNomorMeja();
            int kapasitas = t.getKapasitas();
            
            String status = "KOSONG";
            Reservation activeRes = null;

            for (Reservation r : reservations) {
                if (r.getNomorMeja() == tableNum && "AKTIF".equals(r.getStatus())) {
                    // Cek jika reservasi aktif berada dalam rentang target waktu (+- 120 menit)
                    long diffMinutes = Math.abs(java.time.Duration.between(r.getWaktuReservasi(), targetTime).toMinutes());
                    if (diffMinutes < 120) {
                        status = "AKTIF";
                        activeRes = r;
                        break;
                    }
                }
            }

            Pane box = createTableBox(tableNum, status, activeRes, kapasitas);
            view.getTableGrid().add(box, col, row);

            col++;
            if (col == 4) { // 4 kolom per baris
                col = 0;
                row++;
            }
        }

        // Sinkronisasi/Update Tabel Daftar Booking
        updateDaftarTable(reservations);
    }

    private void updateDaftarTable(List<Reservation> reservations) {
        String searchQuery = view.getTxtSearchDaftar().getText().trim().toLowerCase();
        List<Reservation> filteredList = new ArrayList<>();
        
        for (Reservation r : reservations) {
            boolean matches = true;
            if (!searchQuery.isEmpty()) {
                String name = r.getNamaPelanggan() != null ? r.getNamaPelanggan().toLowerCase() : "";
                String tableNum = String.valueOf(r.getNomorMeja());
                if (!name.contains(searchQuery) && !tableNum.contains(searchQuery)) {
                    matches = false;
                }
            }
            if (matches) {
                filteredList.add(r);
            }
        }
        
        // Urutkan berdasarkan ID menurun agar yang terbaru ada di atas
        filteredList.sort((r1, r2) -> Integer.compare(r2.getId(), r1.getId()));
        
        view.getTblReservasi().setItems(FXCollections.observableArrayList(filteredList));
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
