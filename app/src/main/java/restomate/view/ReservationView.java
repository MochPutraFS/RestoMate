package restomate.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import restomate.controller.ReservationController;
import restomate.model.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReservationView {
    private VBox view;
    private ReservationController controller;
    private int selectedTable = -1;
    
    private VBox tableListContainer;
    private TextField txtNama, txtJam;
    private DatePicker datePicker;
    private Button btnSimpan, btnSelesai;
    private Label lblSelectedTable;

    private Task<Void> pollingTask;
    private Thread pollingThread;

    public ReservationView() {
        controller = new ReservationController();
        createView();
        startPolling();
    }

    private void createView() {
        view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #F4F7FC;");

        Label titleLabel = new Label("Manajemen Reservasi & Meja");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2C3E50;");

        HBox mainLayout = new HBox(30);
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Left Panel: Table List
        VBox listPanel = new VBox(15);
        listPanel.setPrefWidth(450);
        HBox.setHgrow(listPanel, Priority.ALWAYS);
        listPanel.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        HBox listHeader = new HBox();
        listHeader.setAlignment(Pos.CENTER_LEFT);
        Label listTitle = new Label("Daftar Meja");
        listTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnAddTable = new Button("+ Tambah Meja");
        btnAddTable.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAddTable.setOnAction(e -> handleAddTable());
        listHeader.getChildren().addAll(listTitle, spacer, btnAddTable);

        tableListContainer = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(tableListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: white;");

        HBox legendBox = new HBox(15);
        legendBox.setAlignment(Pos.CENTER);
        legendBox.getChildren().addAll(
            createLegend("Kosong", "#2ECC71"),
            createLegend("Reserved", "#F1C40F"),
            createLegend("Dipakai", "#E74C3C")
        );

        listPanel.getChildren().addAll(listHeader, scrollPane, legendBox);

        // Right Panel: Reservation Form
        VBox formPanel = new VBox(15);
        formPanel.setPrefWidth(350);
        formPanel.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label formTitle = new Label("Form Reservasi");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        lblSelectedTable = new Label("Pilih meja pada daftar.");
        lblSelectedTable.setStyle("-fx-text-fill: #7F8C8D; -fx-font-style: italic;");

        txtNama = new TextField();
        txtNama.setPromptText("Nama Pelanggan");
        txtNama.setDisable(true);

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setDisable(true);
        datePicker.setMaxWidth(Double.MAX_VALUE);

        txtJam = new TextField();
        txtJam.setPromptText("Jam (HH:mm) - ex: 14:30");
        txtJam.setDisable(true);

        HBox actionBox = new HBox(10);
        btnSimpan = new Button("Simpan Reservasi");
        btnSimpan.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSimpan.setDisable(true);
        btnSimpan.setOnAction(e -> handleSimpan());

        btnSelesai = new Button("Selesai (Kosongkan)");
        btnSelesai.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSelesai.setDisable(true);
        btnSelesai.setOnAction(e -> handleSelesai());

        actionBox.getChildren().addAll(btnSimpan, btnSelesai);

        formPanel.getChildren().addAll(
            formTitle, lblSelectedTable, 
            new Label("Nama Pelanggan:"), txtNama, 
            new Label("Tanggal:"), datePicker,
            new Label("Jam Reservasi:"), txtJam,
            actionBox
        );

        mainLayout.getChildren().addAll(listPanel, formPanel);
        view.getChildren().addAll(titleLabel, mainLayout);
        refreshUI();
    }

    private HBox createLegend(String text, String colorHex) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER);
        Region colorBox = new Region();
        colorBox.setPrefSize(15, 15);
        colorBox.setStyle("-fx-background-color: " + colorHex + "; -fx-border-color: black;");
        box.getChildren().addAll(colorBox, new Label(text));
        return box;
    }

    private void refreshUI() {
        List<Integer> tables = controller.getTables();
        Map<Integer, Reservation> actives = controller.getActiveReservations();
        
        tableListContainer.getChildren().clear();
        for (int tableNum : tables) {
            tableListContainer.getChildren().add(createTableItem(tableNum, actives.get(tableNum)));
        }
    }

    private HBox createTableItem(int tableNum, Reservation res) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10));
        item.setPrefHeight(60);
        
        String color = "#2ECC71"; // Green
        String statusText = "KOSONG";
        String infoText = "";

        if (res != null) {
            infoText = " - " + res.getNamaPelanggan() + " (" + res.getJamReservasi() + ")";
            if ("Reserved".equals(res.getStatus())) {
                color = "#F1C40F"; // Yellow
                statusText = "RESERVED";
            } else if ("Dipakai".equals(res.getStatus())) {
                color = "#E74C3C"; // Red
                statusText = "DIPAKAI";
            }
        }

        item.setStyle("-fx-background-color: white; -fx-border-color: #ECF0F1; -fx-border-width: 0 0 1 0;");
        if (selectedTable == tableNum) {
            item.setStyle("-fx-background-color: #EBF5FB; -fx-border-color: #3498DB; -fx-border-width: 1; -fx-background-radius: 5;");
        }

        Region statusIndicator = new Region();
        statusIndicator.setPrefSize(12, 12);
        statusIndicator.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6;");

        Label lblMeja = new Label("Meja " + tableNum);
        lblMeja.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        Label lblStatus = new Label(statusText + infoText);
        lblStatus.setFont(Font.font("Segoe UI", 12));
        lblStatus.setStyle("-fx-text-fill: #7F8C8D;");

        VBox textContainer = new VBox(2, lblMeja, lblStatus);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        Button btnDelete = new Button("Hapus");
        btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #E74C3C; -fx-font-size: 11px;");
        btnDelete.setOnAction(e -> handleDeleteTable(tableNum));

        item.getChildren().addAll(statusIndicator, textContainer, btnDelete);
        item.setOnMouseClicked(e -> selectTable(tableNum));
        
        return item;
    }

    private void selectTable(int tableNum) {
        selectedTable = tableNum;
        lblSelectedTable.setText("Meja Terpilih: " + tableNum);
        
        Map<Integer, Reservation> actives = controller.getActiveReservations();
        Reservation res = actives.get(tableNum);
        
        if (res == null) {
            txtNama.setDisable(false);
            datePicker.setDisable(false);
            txtJam.setDisable(false);
            txtNama.clear();
            datePicker.setValue(LocalDate.now());
            txtJam.setText(LocalTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("HH:mm")));
            
            btnSimpan.setDisable(false);
            btnSelesai.setDisable(true);
        } else {
            txtNama.setDisable(true);
            datePicker.setDisable(true);
            txtJam.setDisable(true);
            txtNama.setText(res.getNamaPelanggan());
            if (res.getTanggalReservasi() != null) {
                datePicker.setValue(LocalDate.parse(res.getTanggalReservasi()));
            }
            txtJam.setText(res.getJamReservasi());
            
            btnSimpan.setDisable(true);
            btnSelesai.setDisable(false);
        }
        refreshUI();
    }

    private void handleSimpan() {
        if (selectedTable == -1) return;
        String nama = txtNama.getText();
        LocalDate tgl = datePicker.getValue();
        String jam = txtJam.getText();
        
        if (nama.isEmpty() || tgl == null || jam.isEmpty()) {
            showAlert("Input Tidak Lengkap", "Mohon isi semua field.");
            return;
        }
        
        if (controller.addReservation(nama, selectedTable, tgl.toString(), jam)) {
            txtNama.clear();
            txtJam.clear();
            txtNama.setDisable(true);
            datePicker.setDisable(true);
            txtJam.setDisable(true);
            btnSimpan.setDisable(true);
            refreshUI();
        } else {
            showAlert("Error", "Gagal menyimpan reservasi.");
        }
    }

    private void handleSelesai() {
        if (selectedTable == -1) return;
        if (controller.finishReservation(selectedTable)) {
            txtNama.clear();
            btnSelesai.setDisable(true);
            refreshUI();
        } else {
            showAlert("Error", "Gagal menyelesaikan reservasi.");
        }
    }

    private void handleAddTable() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Tambah Meja Baru");
        dialog.setHeaderText("Masukkan Nomor Meja Baru");
        dialog.setContentText("Nomor Meja:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(numStr -> {
            try {
                int num = Integer.parseInt(numStr);
                if (controller.addTable(num)) {
                    refreshUI();
                } else {
                    showAlert("Gagal", "Nomor meja sudah ada atau input tidak valid.");
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Input harus berupa angka.");
            }
        });
    }

    private void handleDeleteTable(int tableNum) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Hapus Meja");
        alert.setHeaderText("Hapus Meja " + tableNum + "?");
        alert.setContentText("Aksi ini tidak dapat dibatalkan.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (controller.deleteTable(tableNum)) {
                if (selectedTable == tableNum) selectedTable = -1;
                refreshUI();
            } else {
                showAlert("Gagal", "Gagal menghapus meja.");
            }
        }
    }

    private void startPolling() {
        pollingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!isCancelled()) {
                    controller.autoUpdateStatus();
                    Platform.runLater(() -> refreshUI());
                    Thread.sleep(5000); 
                }
                return null;
            }
        };
        pollingThread = new Thread(pollingTask);
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    public void stopPolling() {
        if (pollingTask != null) pollingTask.cancel();
        if (pollingThread != null) pollingThread.interrupt();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return view;
    }
}