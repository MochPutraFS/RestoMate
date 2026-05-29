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
            chkSelect.setStyle("-fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-font-size: 11px;");
            chkSelect.setCursor(javafx.scene.Cursor.HAND);
            chkSelect.setPrefWidth(130);
            chkSelect.setMinWidth(130);
            chkSelect.setMaxWidth(130);
            
            Label lblStock = new Label(String.valueOf(menu.getStok()));
            lblStock.setStyle("-fx-text-fill: #5D4037; -fx-font-weight: bold; -fx-font-size: 11px;");
            lblStock.setPrefWidth(45);
            lblStock.setMinWidth(45);
            lblStock.setMaxWidth(45);
            lblStock.setAlignment(Pos.CENTER_LEFT);
            
            Label lblPrice = new Label(String.format("Rp%,.0f", menu.getHarga()));
            lblPrice.setStyle("-fx-text-fill: #5D4037; -fx-font-weight: bold; -fx-font-size: 11px;");
            lblPrice.setPrefWidth(60);
            lblPrice.setMinWidth(60);
            lblPrice.setMaxWidth(60);
            lblPrice.setAlignment(Pos.CENTER_RIGHT);
            
            spnQty = new Spinner<>(1, Math.max(1, menu.getStok()), 1);
            spnQty.setPrefWidth(60);
            spnQty.setMinWidth(60);
            spnQty.setMaxWidth(60);
            spnQty.setDisable(true);
            spnQty.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E8DDD0; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 11px;");
            spnQty.getEditor().setStyle("-fx-background-color: transparent; -fx-text-fill: #2C1A0E; -fx-font-size: 11px;");
            
            lblSubtotal = new Label("Rp 0");
            lblSubtotal.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold; -fx-font-size: 12px;");
            lblSubtotal.setPrefWidth(65);
            lblSubtotal.setMinWidth(65);
            lblSubtotal.setMaxWidth(65);
            lblSubtotal.setAlignment(Pos.CENTER_RIGHT);
            
            container = new HBox(6, chkSelect, lblStock, lblPrice, spnQty, lblSubtotal);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(6, 8, 6, 8));
            container.setStyle("-fx-border-color: #FDF6EC; -fx-border-width: 0 0 1 0; -fx-background-color: #FFFFFF;");
            
            // Hover effect and dynamic background based on checkbox state
            container.setOnMouseEntered(e -> {
                if (!chkSelect.isSelected()) {
                    container.setStyle("-fx-border-color: #E8DDD0; -fx-border-width: 0 0 1 0; -fx-background-color: #FDF6EC;");
                }
            });
            container.setOnMouseExited(e -> {
                if (!chkSelect.isSelected()) {
                    container.setStyle("-fx-border-color: #FDF6EC; -fx-border-width: 0 0 1 0; -fx-background-color: #FFFFFF;");
                }
            });
            chkSelect.selectedProperty().addListener((obs, oldVal, newVal) -> {
                spnQty.setDisable(!newVal);
                updateSubtotal();
                if (newVal) {
                    container.setStyle("-fx-border-color: #F5CBA7; -fx-border-width: 0 0 1 0; -fx-background-color: #FFFDF9;");
                } else {
                    container.setStyle("-fx-border-color: #FDF6EC; -fx-border-width: 0 0 1 0; -fx-background-color: #FFFFFF;");
                }
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
        dialog.setHeaderText(null); // Disable standard header to use our custom header

        // Set buttons
        ButtonType btnSimpanType = new ButtonType("Booking Meja", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnBatalType = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSimpanType, btnBatalType);

        // Styling dialog pane
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FDF6EC; -fx-padding: 0;");
        dialogPane.setPrefWidth(390);

        // Custom styled input controls helper
        java.util.function.BiConsumer<Control, Double> styleInputWithWidth = (ctrl, width) -> {
            boolean isComboOrDate = ctrl instanceof ComboBox || ctrl instanceof DatePicker;
            String padding = isComboOrDate ? "3 6" : "7 10";
            String baseStyle = "-fx-background-color: #FFFFFF; -fx-border-color: #E8DDD0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: " + padding + "; -fx-font-size: 12px; -fx-text-fill: #2C1A0E;";
            
            // Set widths via Java API instead of CSS to avoid CSS Double.MAX_VALUE parsing warning
            if (width != null) {
                ctrl.setPrefWidth(width);
                ctrl.setMaxWidth(width);
            } else {
                ctrl.setMaxWidth(Double.MAX_VALUE);
            }
            
            final String normal = baseStyle;
            final String hover = normal.replace("-fx-border-color: #E8DDD0;", "-fx-border-color: #BDC3C7;");
            final String focus = normal.replace("-fx-border-color: #E8DDD0; -fx-border-width: 1;", "-fx-border-color: #C0392B; -fx-border-width: 1.5;") + " -fx-effect: dropshadow(gaussian, rgba(192,57,43,0.15), 6, 0, 0, 0);";
            
            ctrl.setStyle(normal);
            ctrl.setOnMouseEntered(ev -> {
                if (!ctrl.isFocused()) ctrl.setStyle(hover);
            });
            ctrl.setOnMouseExited(ev -> {
                if (!ctrl.isFocused()) ctrl.setStyle(normal);
            });
            ctrl.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    ctrl.setStyle(focus);
                } else {
                    ctrl.setStyle(normal);
                }
            });
        };

        // Custom header
        VBox headerPane = new VBox(4);
        headerPane.setPadding(new Insets(15, 20, 15, 20));
        headerPane.setBackground(new Background(new BackgroundFill(Color.web("#FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
        headerPane.setStyle("-fx-border-color: #E8DDD0; -fx-border-width: 0 0 1 0;");

        Label lblDialogTitle = new Label("Catat Reservasi Baru");
        lblDialogTitle.setStyle("-fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblDialogSubtitle = new Label("Meja " + nomorMeja + " (Kapasitas: " + kapasitas + " Orang)");
        lblDialogSubtitle.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");

        headerPane.getChildren().addAll(lblDialogTitle, lblDialogSubtitle);

        // Fields
        TextField txtNama = new TextField();
        txtNama.setPromptText("Nama pelanggan...");
        styleInputWithWidth.accept(txtNama, null);

        Spinner<Integer> spnJumlahOrang = new Spinner<>(1, 100, 2);
        spnJumlahOrang.setEditable(true);
        styleInputWithWidth.accept(spnJumlahOrang, null);
        spnJumlahOrang.getEditor().setStyle("-fx-background-color: transparent; -fx-text-fill: #2C1A0E; -fx-font-size: 12px;");

        LocalDate defaultDate = LocalDate.now();
        String defaultJam = "12";
        String defaultMenit = "00";
        if (!view.getChkLiveStatus().isSelected()) {
            if (view.getDpFilterTanggal().getValue() != null) {
                defaultDate = view.getDpFilterTanggal().getValue();
            }
            if (view.getCmbFilterJam().getValue() != null) {
                defaultJam = view.getCmbFilterJam().getValue();
            }
            if (view.getCmbFilterMenit().getValue() != null) {
                defaultMenit = view.getCmbFilterMenit().getValue();
            }
        }

        DatePicker dpTanggal = new DatePicker(defaultDate);
        dpTanggal.setPromptText("Pilih Tanggal");
        styleInputWithWidth.accept(dpTanggal, null);

        HBox timeBox = new HBox(8);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cmbJam = new ComboBox<>();
        for (int h = 8; h <= 22; h++) cmbJam.getItems().add(String.format("%02d", h));
        cmbJam.setValue(defaultJam);
        styleInputWithWidth.accept(cmbJam, 82.0); // Width increased to 82 to prevent content truncation

        Label lblSep = new Label(":");
        lblSep.setStyle("-fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-font-size: 13px;");

        ComboBox<String> cmbMenit = new ComboBox<>();
        cmbMenit.getItems().addAll("00", "15", "30", "45");
        cmbMenit.setValue(defaultMenit);
        styleInputWithWidth.accept(cmbMenit, 82.0); // Width increased to 82 to prevent content truncation

        timeBox.getChildren().addAll(cmbJam, lblSep, cmbMenit);

        TextField txtCatatan = new TextField();
        txtCatatan.setPromptText("Pesan khusus (mis: baby chair)...");
        styleInputWithWidth.accept(txtCatatan, null);

        CheckBox chkPreOrder = new CheckBox("Pre-order Menu Makanan");
        chkPreOrder.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-padding: 4 0 4 0; -fx-cursor: hand;");
        chkPreOrder.setOnMouseEntered(ev -> chkPreOrder.setStyle("-fx-font-size: 12px; -fx-text-fill: #C0392B; -fx-font-weight: bold; -fx-padding: 4 0 4 0; -fx-cursor: hand;"));
        chkPreOrder.setOnMouseExited(ev -> chkPreOrder.setStyle("-fx-font-size: 12px; -fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-padding: 4 0 4 0; -fx-cursor: hand;"));

        // Right Column for Pre-order Menu (styled as a card)
        VBox rightPane = new VBox(12);
        rightPane.setPadding(new Insets(15));
        rightPane.setBackground(new Background(new BackgroundFill(Color.web("#FFFFFF"), new CornerRadii(12), Insets.EMPTY)));
        rightPane.setStyle("-fx-border-color: #E8DDD0; -fx-border-width: 1; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");
        rightPane.setPrefWidth(430);
        rightPane.setMinWidth(430);
        rightPane.setVisible(false);
        rightPane.setManaged(false);

        // Pre-order menu search & list
        TextField txtCariMenu = new TextField();
        txtCariMenu.setPromptText("Cari menu pre-order...");
        styleInputWithWidth.accept(txtCariMenu, null);
        
        VBox vboxMenuList = new VBox(2);
        vboxMenuList.setStyle("-fx-background-color: white;");
        
        List<PreOrderMenuRow> menuRows = new ArrayList<>();
        List<com.restomate.models.MenuRestoran> dbMenus = new com.restomate.dao.MenuDAO().getAllMenus();
        for (com.restomate.models.MenuRestoran m : dbMenus) {
            if (m.getStok() > 0) {
                PreOrderMenuRow row = new PreOrderMenuRow(m);
                menuRows.add(row);
                vboxMenuList.getChildren().add(row.container);
            }
        }
        
        // Beautiful Table Header for Pre-order menu
        HBox tableHeader = new HBox(6);
        tableHeader.setPadding(new Insets(6, 8, 6, 8));
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        tableHeader.setStyle("-fx-background-color: #FDF6EC; -fx-border-color: #E8DDD0; -fx-border-width: 1 1 0 1; -fx-border-radius: 6 6 0 0; -fx-background-radius: 6 6 0 0;");
        
        Label thMenu = new Label("Menu");
        thMenu.setStyle("-fx-text-fill: #4A3C31; -fx-font-weight: bold; -fx-font-size: 11px;");
        thMenu.setPrefWidth(130);
        
        Label thStok = new Label("Stok");
        thStok.setStyle("-fx-text-fill: #4A3C31; -fx-font-weight: bold; -fx-font-size: 11px;");
        thStok.setPrefWidth(45);
        thStok.setAlignment(Pos.CENTER_LEFT);
        
        Label thHarga = new Label("Harga");
        thHarga.setStyle("-fx-text-fill: #4A3C31; -fx-font-weight: bold; -fx-font-size: 11px;");
        thHarga.setPrefWidth(60);
        thHarga.setAlignment(Pos.CENTER_RIGHT);
        
        Label thJumlah = new Label("Jumlah");
        thJumlah.setStyle("-fx-text-fill: #4A3C31; -fx-font-weight: bold; -fx-font-size: 11px;");
        thJumlah.setPrefWidth(60);
        thJumlah.setAlignment(Pos.CENTER);
        
        Label thSubtotal = new Label("Subtotal");
        thSubtotal.setStyle("-fx-text-fill: #4A3C31; -fx-font-weight: bold; -fx-font-size: 11px;");
        thSubtotal.setPrefWidth(65);
        thSubtotal.setAlignment(Pos.CENTER_RIGHT);
        
        tableHeader.getChildren().addAll(thMenu, thStok, thHarga, thJumlah, thSubtotal);
        
        ScrollPane scrollMenuList = new ScrollPane(vboxMenuList);
        scrollMenuList.setFitToWidth(true);
        scrollMenuList.setPrefHeight(160);
        scrollMenuList.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: #E8DDD0; -fx-border-width: 0 1 1 1; -fx-border-radius: 0 0 8 8; -fx-background-radius: 0 0 8 8;");
        
        Label lblTotalEstimation = new Label("Estimasi Total Pre-Order: Rp 0");
        lblTotalEstimation.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold; -fx-font-size: 12px;");
        
        // Wrap total estimation in a beautiful alert-style pill box
        HBox totalEstBox = new HBox(lblTotalEstimation);
        totalEstBox.setAlignment(Pos.CENTER_LEFT);
        totalEstBox.setPadding(new Insets(8, 12, 8, 12));
        totalEstBox.setBackground(new Background(new BackgroundFill(Color.web("#FDEDEC"), new CornerRadii(8), Insets.EMPTY)));
        totalEstBox.setStyle("-fx-border-color: #F5B7B1; -fx-border-width: 1; -fx-border-radius: 8;");

        // DP Payment fields
        Label lblBiayaTotal = new Label("Rp 0");
        lblBiayaTotal.setStyle("-fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label lblMinDP = new Label("Rp 0");
        lblMinDP.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold; -fx-font-size: 13px;");

        TextField txtDPPaid = new TextField("0");
        txtDPPaid.setPromptText("Masukkan nominal DP...");
        styleInputWithWidth.accept(txtDPPaid, null);
        txtDPPaid.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtDPPaid.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        ComboBox<String> cmbDPPayment = new ComboBox<>();
        cmbDPPayment.getItems().addAll("CASH", "QRIS");
        cmbDPPayment.setValue("CASH");
        styleInputWithWidth.accept(cmbDPPayment, null);

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
            if (chkPreOrder.isSelected()) {
                for (PreOrderMenuRow row : menuRows) {
                    total += row.getSubtotalValue();
                }
            }
            double minDP = total * 0.5;
            java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
            lblTotalEstimation.setText("Estimasi Total Pre-Order: Rp " + formatter.format(total));
            lblBiayaTotal.setText("Rp " + formatter.format(total));
            lblMinDP.setText("Rp " + formatter.format(minDP));
            txtDPPaid.setText(String.valueOf((int) minDP));
        };
        
        // Add listeners to all rows to trigger grand total updates
        for (PreOrderMenuRow row : menuRows) {
            row.chkSelect.selectedProperty().addListener((obs, oldVal, newVal) -> updateGrandTotal.run());
            row.spnQty.valueProperty().addListener((obs, oldVal, newVal) -> updateGrandTotal.run());
        }

        chkPreOrder.selectedProperty().addListener((obs, oldVal, newVal) -> {
            rightPane.setVisible(newVal);
            rightPane.setManaged(newVal);
            if (newVal) {
                dialogPane.setPrefWidth(830);
            } else {
                dialogPane.setPrefWidth(390);
            }
            updateGrandTotal.run();
            if (dialogPane.getScene() != null && dialogPane.getScene().getWindow() != null) {
                dialogPane.getScene().getWindow().sizeToScene();
            }
        });

        HBox readyTimeBox = new HBox(8);
        readyTimeBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cmbReadyJam = new ComboBox<>();
        for (int h = 8; h <= 22; h++) cmbReadyJam.getItems().add(String.format("%02d", h));
        cmbReadyJam.setValue("12");
        styleInputWithWidth.accept(cmbReadyJam, 82.0); // Width increased to 82 to prevent content truncation

        Label lblReadySep = new Label(":");
        lblReadySep.setStyle("-fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-font-size: 13px;");

        ComboBox<String> cmbReadyMenit = new ComboBox<>();
        cmbReadyMenit.getItems().addAll("00", "15", "30", "45");
        cmbReadyMenit.setValue("00");
        styleInputWithWidth.accept(cmbReadyMenit, 82.0); // Width increased to 82 to prevent content truncation

        readyTimeBox.getChildren().addAll(cmbReadyJam, lblReadySep, cmbReadyMenit);

        Label lblMenuTitle = new Label("🍽️ Pilih Menu Pre-Order");
        lblMenuTitle.setStyle("-fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-font-size: 14px;");

        rightPane.getChildren().addAll(
                lblMenuTitle,
                txtCariMenu,
                tableHeader,
                scrollMenuList,
                totalEstBox,
                makeLabel("Waktu Makanan Siap (Ready Time):"),
                readyTimeBox
        );

        // Left Column (Informasi Pelanggan & DP)
        VBox leftPane = new VBox(12);
        leftPane.setPrefWidth(350);
        leftPane.setMinWidth(350);

        // Card 1: Customer Information
        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(15));
        formCard.setBackground(new Background(new BackgroundFill(Color.web("#FFFFFF"), new CornerRadii(12), Insets.EMPTY)));
        formCard.setStyle("-fx-border-color: #E8DDD0; -fx-border-width: 1; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");

        Label lblPlgTitle = new Label("📝 Informasi Pelanggan");
        lblPlgTitle.setStyle("-fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-font-size: 13px;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.TOP_LEFT);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(110);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        formGrid.getColumnConstraints().addAll(col1, col2);

        formGrid.add(makeLabel("Nama Pelanggan:"), 0, 0);
        formGrid.add(txtNama, 1, 0);
        formGrid.add(makeLabel("Jumlah Orang:"), 0, 1);
        formGrid.add(spnJumlahOrang, 1, 1);
        formGrid.add(makeLabel("Tanggal:"), 0, 2);
        formGrid.add(dpTanggal, 1, 2);
        formGrid.add(makeLabel("Waktu Reservasi:"), 0, 3);
        formGrid.add(timeBox, 1, 3);
        formGrid.add(makeLabel("Catatan (Khusus):"), 0, 4);
        formGrid.add(txtCatatan, 1, 4);

        formCard.getChildren().addAll(lblPlgTitle, formGrid);

        // Card 2: DP Card (SaaS Billing style)
        VBox dpCard = new VBox(12);
        dpCard.setPadding(new Insets(15));
        dpCard.setBackground(new Background(new BackgroundFill(Color.web("#FFFDF9"), new CornerRadii(12), Insets.EMPTY)));
        dpCard.setStyle("-fx-border-color: #F5CBA7; -fx-border-width: 1.5; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 8, 0, 0, 2);");

        Label lblDPTitle = new Label("💰 Pembayaran DP Reservasi");
        lblDPTitle.setStyle("-fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-font-size: 13px;");

        HBox summaryBox = new HBox(10);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setPadding(new Insets(10, 8, 10, 8));
        summaryBox.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E8DDD0; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        VBox totalCol = new VBox(4);
        totalCol.setAlignment(Pos.CENTER);
        Label lblTotalTitle = new Label("Total Biaya");
        lblTotalTitle.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 10px;");
        totalCol.getChildren().addAll(lblTotalTitle, lblBiayaTotal);
        
        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        sep.setPrefHeight(25);
        
        VBox dpCol = new VBox(4);
        dpCol.setAlignment(Pos.CENTER);
        Label lblMinDPTitle = new Label("Minimal DP (50%)");
        lblMinDPTitle.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 10px;");
        dpCol.getChildren().addAll(lblMinDPTitle, lblMinDP);
        
        HBox.setHgrow(totalCol, Priority.ALWAYS);
        HBox.setHgrow(dpCol, Priority.ALWAYS);
        summaryBox.getChildren().addAll(totalCol, sep, dpCol);

        GridPane dpInputGrid = new GridPane();
        dpInputGrid.setHgap(10);
        dpInputGrid.setVgap(10);
        dpInputGrid.setAlignment(Pos.TOP_LEFT);

        ColumnConstraints dpCol1 = new ColumnConstraints();
        dpCol1.setMinWidth(90);
        ColumnConstraints dpCol2 = new ColumnConstraints();
        dpCol2.setHgrow(Priority.ALWAYS);
        dpInputGrid.getColumnConstraints().addAll(dpCol1, dpCol2);

        dpInputGrid.add(makeLabel("Nominal DP:"), 0, 0);
        dpInputGrid.add(txtDPPaid, 1, 0);
        dpInputGrid.add(makeLabel("Metode DP:"), 0, 1);
        dpInputGrid.add(cmbDPPayment, 1, 1);

        dpCard.getChildren().addAll(lblDPTitle, summaryBox, dpInputGrid);

        leftPane.getChildren().addAll(formCard, chkPreOrder, dpCard);

        HBox bodyContainer = new HBox(20);
        bodyContainer.setPadding(new Insets(15));
        bodyContainer.getChildren().addAll(leftPane, rightPane);

        VBox dialogRoot = new VBox(0);
        dialogRoot.setStyle("-fx-background-color: #FDF6EC;");
        dialogRoot.getChildren().addAll(headerPane, bodyContainer);

        dialogPane.setContent(dialogRoot);

        // Validation & Action filter on "Booking Meja"
        Button btnSimpan = (Button) dialogPane.lookupButton(btnSimpanType);
        
        // Custom styling for default dialog buttons
        if (btnSimpan != null) {
            String normalStyle = "-fx-background-color: #C0392B; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;";
            String hoverStyle = "-fx-background-color: #E74C3C; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;";
            btnSimpan.setStyle(normalStyle);
            btnSimpan.setOnMouseEntered(ev -> btnSimpan.setStyle(hoverStyle));
            btnSimpan.setOnMouseExited(ev -> btnSimpan.setStyle(normalStyle));
        }

        Button btnBatal = (Button) dialogPane.lookupButton(btnBatalType);
        if (btnBatal != null) {
            String normalStyle = "-fx-background-color: #FFFFFF; -fx-text-fill: #7F8C8D; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #BDC3C7; -fx-border-radius: 8; -fx-cursor: hand;";
            String hoverStyle = "-fx-background-color: #F2F4F4; -fx-text-fill: #2C3E50; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-border-color: #7F8C8D; -fx-border-radius: 8; -fx-cursor: hand;";
            btnBatal.setStyle(normalStyle);
            btnBatal.setOnMouseEntered(ev -> btnBatal.setStyle(hoverStyle));
            btnBatal.setOnMouseExited(ev -> btnBatal.setStyle(normalStyle));
        }

        // Style the button bar container if found
        ButtonBar buttonBar = (ButtonBar) dialogPane.lookup(".button-bar");
        if (buttonBar != null) {
            buttonBar.setStyle("-fx-background-color: #FDF6EC; -fx-padding: 10px 20px 15px 20px;");
        }

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

                // DP Validation
                double totalCost = 0;
                if (chkPreOrder.isSelected()) {
                    for (PreOrderMenuRow row : menuRows) {
                        totalCost += row.getSubtotalValue();
                    }
                }
                double minDP = totalCost * 0.5;
                double dpPaid = 0;
                try {
                    String dpPaidText = txtDPPaid.getText().trim();
                    dpPaid = dpPaidText.isEmpty() ? 0 : Double.parseDouble(dpPaidText);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "Nominal Salah", "Nominal DP harus berupa angka!");
                    event.consume();
                    return;
                }

                if (dpPaid < minDP) {
                    showAlert(Alert.AlertType.WARNING, "DP Kurang", "Nominal DP yang dibayarkan kurang dari 50% total biaya reservasi (Min: Rp " + minDP + ")!");
                    event.consume();
                    return;
                }

                // Save DP Transaction to DB
                if (dpPaid > 0) {
                    com.restomate.models.Transaction dpTx = new com.restomate.models.Transaction(
                        0, dpPaid, cmbDPPayment.getValue(),
                        "[DP Reservasi Meja " + nomorMeja + " (Pelanggan: " + nama + ")]",
                        LocalDateTime.now()
                    );
                    dpTx.setNamaPelanggan(nama);
                    dpTx.setTipePesanan("RESERVASI");
                    List<com.restomate.models.TransactionItem> emptyItems = new ArrayList<>();
                    new com.restomate.dao.TransactionDAO().saveTransaction(dpTx, emptyItems);
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
                
                Reservation r = new Reservation(0, nama, nomorMeja, datetime, "AKTIF", jumlahOrang, menuDipesan, catatan, waktuSiap, totalCost, dpPaid);
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
        lbl.setStyle("-fx-text-fill: #4A3C31; -fx-font-weight: bold; -fx-font-size: 12px;");
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
        double width = 102;
        double height = 102;
        String shapeStyle = "";
        
        if (kapasitas <= 3) {
            // Meja Bulat
            width = 102;
            height = 102;
            shapeStyle = "-fx-background-radius: 51; -fx-border-radius: 51;";
        } else if (kapasitas == 4) {
            // Meja Kotak
            width = 102;
            height = 102;
            shapeStyle = "-fx-background-radius: 12; -fx-border-radius: 12;";
        } else {
            // Meja Panjang (Kapasitas > 4)
            width = 132;
            height = 102;
            shapeStyle = "-fx-background-radius: 12; -fx-border-radius: 12;";
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
        lblNo.setStyle("-fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-font-size: 15px;");
        
        String statusLabelText = "Tersedia";
        if ("AKTIF".equals(status)) {
            if (r != null && "CHECKED_IN".equals(r.getStatus())) {
                statusLabelText = "Terisi";
            } else {
                statusLabelText = "Dipesan";
            }
        }
        
        Label lblStatus = new Label(statusLabelText);
        lblStatus.setStyle("-fx-text-fill: " + textFillColor + "; -fx-font-weight: bold; -fx-font-size: 11px;");
        
        Label lblCap = new Label("Kapasitas: " + kapasitas);
        lblCap.setStyle("-fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 10px;");
        
        Label lblNama = new Label("AKTIF".equals(status) && r != null ? r.getNamaPelanggan() : "");
        lblNama.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold; -fx-font-size: 11px;");
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
            
            if ("CHECKED_IN".equals(r.getStatus())) {
                sb.append("Status: Sedang Makan (Checked-In)\n");
            } else {
                sb.append("Status: Dipesan (Aktif)\n");
            }
            
            if (r.getMenuDipesan() != null && !r.getMenuDipesan().trim().isEmpty()) {
                sb.append("Menu Pre-order: ").append(r.getMenuDipesan()).append("\n");
                if (r.getWaktuSiap() != null && !r.getWaktuSiap().trim().isEmpty()) {
                    sb.append("Waktu Siap: ").append(r.getWaktuSiap()).append("\n");
                }
            }
            if (r.getCatatan() != null && !r.getCatatan().trim().isEmpty()) {
                sb.append("Catatan: ").append(r.getCatatan()).append("\n");
            }
            sb.append("Waktu Reservasi: ").append(r.getWaktuReservasi() != null ? r.getWaktuReservasi().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : "-").append("\n");
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
        details.append("• Waktu Reservasi: ").append(r.getWaktuReservasi() != null ? r.getWaktuReservasi().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : "-").append("\n");
        details.append("• Jumlah Tamu:    ").append(r.getJumlahOrang()).append(" Orang\n");
        
        if (r.getMenuDipesan() != null && !r.getMenuDipesan().trim().isEmpty()) {
            details.append("• Menu Pre-order: ").append(r.getMenuDipesan()).append("\n");
            if (r.getWaktuSiap() != null && !r.getWaktuSiap().trim().isEmpty()) {
                details.append("• Ready Time:     Pukul ").append(r.getWaktuSiap()).append("\n");
            }
        } else {
            details.append("• Menu Pre-order: -\n");
        }
        
        details.append("• Catatan Khusus: ").append((r.getCatatan() != null && !r.getCatatan().trim().isEmpty()) ? r.getCatatan() : "-").append("\n");
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
        details.append("• Total Biaya:    Rp ").append(formatter.format(r.getBiayaTotal())).append("\n");
        details.append("• DP Terbayar:    Rp ").append(formatter.format(r.getDpDibayar())).append("\n\n");

        // Jika statusnya bukan AKTIF atau CHECKED_IN, tampilkan dalam bentuk dialog info read-only
        if (!"AKTIF".equals(r.getStatus()) && !"CHECKED_IN".equals(r.getStatus())) {
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Detail Reservasi - Meja " + r.getNomorMeja());
            infoAlert.setHeaderText("Reservasi Meja " + r.getNomorMeja() + " oleh: " + r.getNamaPelanggan() + " (" + r.getStatus() + ")");
            infoAlert.setContentText(details.toString());
            infoAlert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kelola Reservasi - Meja " + r.getNomorMeja());
        
        if ("CHECKED_IN".equals(r.getStatus())) {
            alert.setHeaderText("Reservasi Meja " + r.getNomorMeja() + " oleh: " + r.getNamaPelanggan() + " (SEDANG MAKAN / CHECKED-IN)");
        } else {
            alert.setHeaderText("Reservasi Meja " + r.getNomorMeja() + " oleh: " + r.getNamaPelanggan());
        }
        
        details.append("Silakan tentukan tindakan selanjutnya:");
        alert.setContentText(details.toString());

        // Buttons for active bookings
        ButtonType btnCheckIn = new ButtonType("Check-In (POS)");
        ButtonType btnSelesai = new ButtonType("Selesaikan (Meja Kosong)");
        ButtonType btnBatalBooking = new ButtonType("Batalkan Booking");
        ButtonType btnClose = new ButtonType("Kembali", ButtonBar.ButtonData.CANCEL_CLOSE);

        if ("CHECKED_IN".equals(r.getStatus())) {
            alert.getButtonTypes().setAll(btnSelesai, btnClose);
        } else {
            alert.getButtonTypes().setAll(btnCheckIn, btnSelesai, btnBatalBooking, btnClose);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnCheckIn) {
                // Check-In POS Action -> Ubah status jadi CHECKED_IN agar tetap terisi di denah
                if (dao.updateStatus(r.getId(), "CHECKED_IN")) {
                    refreshVisuals();
                    if (DashboardView.getInstance() != null) {
                        DashboardView.getInstance().switchToCashierAndLoadPreOrder(r.getId(), r.getNamaPelanggan(), r.getMenuDipesan(), r.getDpDibayar());
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Peringatan", "Gagal memindahkan ke Kasir karena dashboard tidak aktif.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal melakukan Check-In.");
                }
            } else if (result.get() == btnSelesai) {
                // Warning jika dibypass langsung dari dialog tanpa lewat kasir untuk pre-order
                if (r.getBiayaTotal() > 0 && "AKTIF".equals(r.getStatus())) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Peringatan Selesaikan Reservasi");
                    confirm.setHeaderText("Reservasi Memiliki Pre-order");
                    confirm.setContentText("Reservasi ini memiliki menu pre-order senilai Rp " + formatter.format(r.getBiayaTotal()) + 
                            " yang belum dibayar di kasir. Menyelesaikan reservasi langsung akan melewati pembayaran kasir POS. Tetap selesaikan manual?");
                    Optional<ButtonType> confResult = confirm.showAndWait();
                    if (confResult.isEmpty() || confResult.get() != ButtonType.OK) {
                        return;
                    }
                }
                
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
                if (r.getNomorMeja() == tableNum) {
                    if (r.getWaktuReservasi() == null) continue;
                    
                    // Jika statusnya CHECKED_IN, maka selama tanggal reservasi hari ini, meja tetap terisi
                    if ("CHECKED_IN".equals(r.getStatus())) {
                        if (targetTime.toLocalDate().equals(r.getWaktuReservasi().toLocalDate())) {
                            status = "AKTIF";
                            activeRes = r;
                            break;
                        }
                    }
                    
                    // Untuk status AKTIF, tetap cek bentrok 120 menit
                    if ("AKTIF".equals(r.getStatus())) {
                        long diffMinutes = Math.abs(java.time.Duration.between(r.getWaktuReservasi(), targetTime).toMinutes());
                        if (diffMinutes < 120) {
                            status = "AKTIF";
                            activeRes = r;
                            break;
                        }
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
