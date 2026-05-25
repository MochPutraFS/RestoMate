package com.restomate.views;

import com.restomate.controllers.ReservationController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;

public class ReservationView {
    private BorderPane root;
    private ReservationController controller;
    
    // UI Form Reservasi
    private TextField txtNama, txtMeja;
    private DatePicker dpTanggal;
    private ComboBox<String> cmbJam;
    private ComboBox<String> cmbMenit;
    private Button btnSimpan;
    
    // UI Form Kelola Meja
    private TextField txtCustomMeja;
    private Button btnTambahMeja;
    private Button btnHapusMeja;
    
    // UI Visualisasi Meja
    private GridPane tableGrid;

    public ReservationView() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Sisi Kiri: Gabungan Form Booking & Kelola Meja
        VBox leftPane = new VBox(20);
        leftPane.setPrefWidth(300);
        
        VBox leftForm = buildForm();
        VBox manageTable = buildManageTableCard();
        leftPane.getChildren().addAll(leftForm, manageTable);
        
        root.setLeft(leftPane);
        
        // Sisi Tengah/Kanan: Visualisasi meja
        VBox rightVisual = buildVisualArea();
        root.setCenter(rightVisual);
        
        // Panggil controller
        controller = new ReservationController(this);
    }
    
    private VBox buildForm() {
        VBox formCard = new VBox(15);
        formCard.setPadding(new Insets(20));
        formCard.setPrefWidth(300);
        formCard.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.05));
        shadow.setRadius(10);
        formCard.setEffect(shadow);
        
        Label lblTitle = new Label("Catat Reservasi");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTitle.setTextFill(Color.web("#333333"));
        
        txtNama = new TextField();
        txtNama.setPromptText("Nama Pelanggan");
        txtNama.setStyle("-fx-font-size: 14px;");
        
        txtMeja = new TextField();
        txtMeja.setPromptText("Nomor Meja (Pilih dari grid)");
        txtMeja.setStyle("-fx-font-size: 14px; -fx-background-color: #F5F5F5;");
        txtMeja.setEditable(false); // Hanya bisa diisi dengan mengklik meja secara visual
        
        dpTanggal = new DatePicker();
        dpTanggal.setPromptText("Pilih Tanggal");
        dpTanggal.setMaxWidth(Double.MAX_VALUE);
        dpTanggal.setStyle("-fx-font-size: 14px;");
        
        // Waktu via ComboBox
        HBox timeBox = new HBox(10);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        
        cmbJam = new ComboBox<>();
        for (int h = 8; h <= 22; h++) {
            cmbJam.getItems().add(String.format("%02d", h));
        }
        cmbJam.setValue("12");
        cmbJam.setStyle("-fx-font-size: 14px; -fx-pref-width: 85px;");
        
        Label lblSep = new Label(":");
        lblSep.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        cmbMenit = new ComboBox<>();
        cmbMenit.getItems().addAll("00", "15", "30", "45");
        cmbMenit.setValue("00");
        cmbMenit.setStyle("-fx-font-size: 14px; -fx-pref-width: 85px;");
        
        timeBox.getChildren().addAll(cmbJam, lblSep, cmbMenit);
        
        btnSimpan = new Button("Booking Meja!");
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        btnSimpan.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-font-size: 14px; -fx-background-radius: 5;");
        btnSimpan.setOnMouseEntered(e -> btnSimpan.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;"));
        btnSimpan.setOnMouseExited(e -> btnSimpan.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-font-size: 14px; -fx-background-radius: 5;"));
        
        formCard.getChildren().addAll(lblTitle, 
                                      new Label("Nama Pelanggan:"), txtNama, 
                                      new Label("Pilih Meja:"), txtMeja, 
                                      new Label("Tanggal:"), dpTanggal, 
                                      new Label("Waktu/Jam:"), timeBox, 
                                      btnSimpan);
        return formCard;
    }
    
    private VBox buildManageTableCard() {
        VBox tableCard = new VBox(15);
        tableCard.setPadding(new Insets(20));
        tableCard.setPrefWidth(300);
        tableCard.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.05));
        shadow.setRadius(10);
        tableCard.setEffect(shadow);
        
        Label lblTitle = new Label("⚙️ Kelola Meja");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTitle.setTextFill(Color.web("#333333"));
        
        txtCustomMeja = new TextField();
        txtCustomMeja.setPromptText("Contoh: 13");
        txtCustomMeja.setStyle("-fx-font-size: 14px;");
        
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);
        
        btnTambahMeja = new Button("➕ Tambah");
        btnTambahMeja.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnTambahMeja, Priority.ALWAYS);
        btnTambahMeja.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");
        btnTambahMeja.setOnMouseEntered(e -> btnTambahMeja.setStyle("-fx-background-color: #45A049; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5; -fx-cursor: hand;"));
        btnTambahMeja.setOnMouseExited(e -> btnTambahMeja.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;"));
        
        btnHapusMeja = new Button("🗑️ Hapus");
        btnHapusMeja.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnHapusMeja, Priority.ALWAYS);
        btnHapusMeja.setStyle("-fx-background-color: #FF5252; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");
        btnHapusMeja.setOnMouseEntered(e -> btnHapusMeja.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5; -fx-cursor: hand;"));
        btnHapusMeja.setOnMouseExited(e -> btnHapusMeja.setStyle("-fx-background-color: #FF5252; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;"));
        
        btnBox.getChildren().addAll(btnTambahMeja, btnHapusMeja);
        
        tableCard.getChildren().addAll(lblTitle, new Label("Nomor Meja:"), txtCustomMeja, btnBox);
        return tableCard;
    }
    
    private VBox buildVisualArea() {
        VBox visualBox = new VBox(15);
        visualBox.setPadding(new Insets(20));
        visualBox.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.05));
        shadow.setRadius(10);
        visualBox.setEffect(shadow);
        
        Label lblTitle = new Label("Ketersediaan Meja (Live 🔴)");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTitle.setTextFill(Color.web("#333333"));
        
        // ScrollPane agar jika meja sangat banyak (misal puluhan meja), UI tidak pecah
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        tableGrid = new GridPane();
        tableGrid.setHgap(15);
        tableGrid.setVgap(15);
        tableGrid.setAlignment(Pos.TOP_CENTER);
        
        scrollPane.setContent(tableGrid);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        visualBox.getChildren().addAll(lblTitle, scrollPane);
        return visualBox;
    }

    public BorderPane getView() { return root; }
    public TextField getTxtNama() { return txtNama; }
    public TextField getTxtMeja() { return txtMeja; }
    public DatePicker getDpTanggal() { return dpTanggal; }
    public ComboBox<String> getCmbJam() { return cmbJam; }
    public ComboBox<String> getCmbMenit() { return cmbMenit; }
    public Button getBtnSimpan() { return btnSimpan; }
    public TextField getTxtCustomMeja() { return txtCustomMeja; }
    public Button getBtnTambahMeja() { return btnTambahMeja; }
    public Button getBtnHapusMeja() { return btnHapusMeja; }
    public GridPane getTableGrid() { return tableGrid; }
    public ReservationController getController() { return controller; }
}
