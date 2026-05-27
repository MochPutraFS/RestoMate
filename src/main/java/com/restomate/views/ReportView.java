package com.restomate.views;

import com.restomate.controllers.ReportController;
import com.restomate.models.Transaction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.chart.*;

public class ReportView {
    private BorderPane root;
    private ReportController controller;
    
    // UI Filter Tanggal
    private DatePicker dpMulai;
    private DatePicker dpSelesai;
    private Button btnFilter;
    
    // Labels statistik
    private Label lblIncome;        // Total Omset
    private Label lblCashIncome;    // Total Tunai
    private Label lblQrisIncome;    // Total QRIS
    private Label lblTxCount;       // Jumlah Transaksi
    private Label lblAov;           // Rata-rata per Transaksi (Average Order Value)
    
    // Tabel riwayat transaksi
    private TableView<Transaction> tableTx;
    
    // Grafik Analisis
    private BarChart<String, Number> barChart;
    private PieChart pieChart;
    
    // Tombol aksi
    private Button btnCetak;
    private Button btnReset;

    public ReportView() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Bagian Kiri: Filter tanggal & ringkasan statistik & tombol
        VBox leftPane = buildLeftPanel();
        root.setLeft(leftPane);
        
        // Bagian Tengah/Kanan: Tab tabel dan grafik
        VBox centerPane = buildCenterPanel();
        root.setCenter(centerPane);
        
        controller = new ReportController(this);
    }

    private VBox buildLeftPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(320);
        panel.setPadding(new Insets(20));
        panel.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.05));
        shadow.setRadius(10);
        panel.setEffect(shadow);
        
        Label lblTitle = new Label("📊 Laporan Pendapatan");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblTitle.setTextFill(Color.web("#333333"));
        
        // 1. Filter Tanggal Card
        VBox cardFilter = new VBox(10);
        cardFilter.setPadding(new Insets(15));
        cardFilter.setBackground(new Background(new BackgroundFill(Color.web("#F5F5F5"), new CornerRadii(8), Insets.EMPTY)));
        cardFilter.setBorder(new Border(new BorderStroke(Color.web("#E0E0E0"), BorderStrokeStyle.SOLID, new CornerRadii(8), BorderWidths.DEFAULT)));
        
        Label lblFilter = new Label("📅 FILTER RENTANG TANGGAL");
        lblFilter.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        lblFilter.setTextFill(Color.web("#555555"));
        
        dpMulai = new DatePicker();
        dpMulai.setPromptText("Tanggal Mulai");
        dpMulai.setMaxWidth(Double.MAX_VALUE);
        
        dpSelesai = new DatePicker();
        dpSelesai.setPromptText("Tanggal Selesai");
        dpSelesai.setMaxWidth(Double.MAX_VALUE);
        
        btnFilter = new Button("🔍 Terapkan Filter");
        btnFilter.setMaxWidth(Double.MAX_VALUE);
        btnFilter.setStyle("-fx-background-color: #009688; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5;");
        btnFilter.setOnMouseEntered(e -> btnFilter.setStyle("-fx-background-color: #00796B; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5; -fx-cursor: hand;"));
        btnFilter.setOnMouseExited(e -> btnFilter.setStyle("-fx-background-color: #009688; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 5;"));
        
        cardFilter.getChildren().addAll(lblFilter, new Label("Mulai:"), dpMulai, new Label("Sampai:"), dpSelesai, btnFilter);
        
        // 2. Kartu Utama: Total Omset (Hijau Premium)
        VBox cardTotal = new VBox(5);
        cardTotal.setPadding(new Insets(15));
        cardTotal.setAlignment(Pos.CENTER);
        cardTotal.setBackground(new Background(new BackgroundFill(Color.web("#E8F5E9"), new CornerRadii(8), Insets.EMPTY)));
        cardTotal.setBorder(new Border(new BorderStroke(Color.web("#2E7D32", 0.2), BorderStrokeStyle.SOLID, new CornerRadii(8), BorderWidths.DEFAULT)));
        
        Label lblHeader = new Label("RINGKASAN OMSET");
        lblHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblHeader.setTextFill(Color.web("#2E7D32"));
        
        lblIncome = new Label("Rp 0");
        lblIncome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblIncome.setTextFill(Color.web("#2E7D32"));
        
        cardTotal.getChildren().addAll(lblHeader, lblIncome);
        cardTotal.setEffect(shadow);
        
        // 3. Grid Kartu Detail
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        
        VBox cardCash = createSubCard("💵 TUNAI (CASH)", lblCashIncome = new Label("Rp 0"), "#E3F2FD", "#1565C0");
        VBox cardQris = createSubCard("📱 QRIS", lblQrisIncome = new Label("Rp 0"), "#F3E5F5", "#6A1B9A");
        VBox cardTx = createSubCard("🧾 TRANSAKSI", lblTxCount = new Label("0"), "#FFF3E0", "#E65100");
        VBox cardAov = createSubCard("💳 RATA-RATA", lblAov = new Label("Rp 0"), "#E0F2F1", "#004D40");
        
        grid.add(cardCash, 0, 0);
        grid.add(cardQris, 1, 0);
        grid.add(cardTx, 0, 1);
        grid.add(cardAov, 1, 1);
        
        // Auto stretching grid cells
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col, col);
        
        // 4. Tombol Aksi
        VBox btnBox = new VBox(10);
        btnBox.setPadding(new Insets(10, 0, 0, 0));
        
        btnCetak = new Button("🖨️ Cetak Laporan");
        btnCetak.setMaxWidth(Double.MAX_VALUE);
        btnCetak.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-font-size: 14px; -fx-background-radius: 5;");
        btnCetak.setOnMouseEntered(e -> btnCetak.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;"));
        btnCetak.setOnMouseExited(e -> btnCetak.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-font-size: 14px; -fx-background-radius: 5;"));
        
        btnReset = new Button("🔒 Tutup Kasir");
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnReset.setStyle("-fx-background-color: #FF5252; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-font-size: 14px; -fx-background-radius: 5;");
        btnReset.setOnMouseEntered(e -> btnReset.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;"));
        btnReset.setOnMouseExited(e -> btnReset.setStyle("-fx-background-color: #FF5252; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-font-size: 14px; -fx-background-radius: 5;"));
        
        btnBox.getChildren().addAll(btnCetak, btnReset);
        
        panel.getChildren().addAll(lblTitle, cardFilter, cardTotal, grid, btnBox);
        return panel;
    }
    
    private VBox createSubCard(String title, Label lblValue, String bgHex, String textHex) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER);
        card.setBackground(new Background(new BackgroundFill(Color.web(bgHex), new CornerRadii(8), Insets.EMPTY)));
        card.setBorder(new Border(new BorderStroke(Color.web(textHex, 0.2), BorderStrokeStyle.SOLID, new CornerRadii(8), BorderWidths.DEFAULT)));
        
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        lblTitle.setTextFill(Color.web(textHex));
        
        lblValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblValue.setTextFill(Color.web(textHex));
        
        card.getChildren().addAll(lblTitle, lblValue);
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.03));
        shadow.setRadius(5);
        card.setEffect(shadow);
        
        return card;
    }
    
    private VBox buildCenterPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        BorderPane.setMargin(panel, new Insets(0, 0, 0, 20));
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.05));
        shadow.setRadius(10);
        panel.setEffect(shadow);
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 14px;");
        
        // Tab 1: Tabel Audit
        Tab tabTable = new Tab("📋 Riwayat Transaksi");
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(15));
        tableTx = new TableView<>();
        tableTx.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableTx, Priority.ALWAYS);
        tableBox.getChildren().add(tableTx);
        tabTable.setContent(tableBox);
        
        // Tab 2: Grafik Analisis
        Tab tabCharts = new Tab("📊 Grafik Analisis");
        VBox chartBox = new VBox(20);
        chartBox.setPadding(new Insets(15));
        chartBox.setAlignment(Pos.TOP_CENTER);
        
        // BarChart Setup
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Tanggal");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Omset (Rp)");
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Tren Pendapatan Harian");
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(350);
        
        // PieChart Setup
        pieChart = new PieChart();
        pieChart.setTitle("Proporsi Metode Pembayaran");
        pieChart.setPrefHeight(350);
        pieChart.setLegendSide(javafx.geometry.Side.RIGHT);
        
        // Susun side-by-side
        HBox chartsLayout = new HBox(20);
        chartsLayout.setAlignment(Pos.CENTER);
        chartsLayout.getChildren().addAll(barChart, pieChart);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        
        chartBox.getChildren().add(chartsLayout);
        tabCharts.setContent(chartBox);
        
        tabPane.getTabs().addAll(tabTable, tabCharts);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        panel.getChildren().add(tabPane);
        return panel;
    }

    public BorderPane getView() { return root; }
    public Label getLblIncome() { return lblIncome; }
    public Label getLblCashIncome() { return lblCashIncome; }
    public Label getLblQrisIncome() { return lblQrisIncome; }
    public Label getLblTxCount() { return lblTxCount; }
    public Label getLblAov() { return lblAov; }
    public TableView<Transaction> getTableTx() { return tableTx; }
    public Button getBtnCetak() { return btnCetak; }
    public Button getBtnReset() { return btnReset; }
    public DatePicker getDpMulai() { return dpMulai; }
    public DatePicker getDpSelesai() { return dpSelesai; }
    public Button getBtnFilter() { return btnFilter; }
    public BarChart<String, Number> getBarChart() { return barChart; }
    public PieChart getPieChart() { return pieChart; }
    public ReportController getController() { return controller; }
}
