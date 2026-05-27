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

    private TextField txtNama, txtMeja;
    private DatePicker dpTanggal;
    private ComboBox<String> cmbJam, cmbMenit;
    private Button btnSimpan;

    private TextField txtCustomMeja;
    private Button btnTambahMeja, btnHapusMeja;

    private GridPane tableGrid;

    // ── Warna Tema Restoran ──
    private static final String CLR_PRIMARY = "#C0392B";
    private static final String CLR_BG      = "#FDF6EC";
    private static final String CLR_DARK    = "#2C1A0E";
    private static final String CLR_BORDER  = "#E8DDD0";

    public ReservationView() {
        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
                Color.web(CLR_BG), CornerRadii.EMPTY, Insets.EMPTY)));

        // Header
        root.setTop(buildHeader());

        // Kiri: Form booking + kelola meja
        VBox leftPane = new VBox(15);
        leftPane.setPadding(new Insets(10, 0, 10, 10));
        leftPane.setPrefWidth(265);
        leftPane.getChildren().addAll(buildForm(), buildManageTableCard());
        root.setLeft(leftPane);

        // Tengah: Visualisasi meja
        VBox rightVisual = buildVisualArea();
        BorderPane.setMargin(rightVisual, new Insets(10, 10, 10, 8));
        root.setCenter(rightVisual);

        controller = new ReservationController(this);
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setStyle("-fx-border-color: " + CLR_BORDER + "; -fx-border-width: 0 0 1 0;");

        Label lblTitle = new Label("📅 Reservasi Meja");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblTitle.setTextFill(Color.web(CLR_DARK));

        header.getChildren().add(lblTitle);
        return header;
    }

    private VBox buildForm() {
        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(12));
        formCard.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        formCard.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);" +
                "-fx-border-color: " + CLR_BORDER + ";" +
                "-fx-border-radius: 12;");

        Label lblTitle = new Label("📝 Catat Reservasi");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTitle.setTextFill(Color.web(CLR_DARK));

        // Garis dekoratif
        javafx.scene.shape.Rectangle line = new javafx.scene.shape.Rectangle(45, 3);
        line.setFill(Color.web(CLR_PRIMARY));

        String fieldStyle =
                "-fx-font-size: 12px; -fx-background-radius: 7; -fx-border-radius: 7;" +
                "-fx-border-color: " + CLR_BORDER + "; -fx-padding: 7;";

        txtNama = new TextField();
        txtNama.setPromptText("Nama pelanggan...");
        txtNama.setStyle(fieldStyle);
        txtNama.setMaxWidth(Double.MAX_VALUE);

        txtMeja = new TextField();
        txtMeja.setPromptText("Klik meja di sebelah kanan →");
        txtMeja.setStyle(fieldStyle + "-fx-background-color: #F8F0E8;");
        txtMeja.setEditable(false);
        txtMeja.setMaxWidth(Double.MAX_VALUE);

        dpTanggal = new DatePicker();
        dpTanggal.setPromptText("Pilih Tanggal");
        dpTanggal.setMaxWidth(Double.MAX_VALUE);
        dpTanggal.setStyle("-fx-font-size: 12px;");

        HBox timeBox = new HBox(8);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        cmbJam = new ComboBox<>();
        for (int h = 8; h <= 22; h++) cmbJam.getItems().add(String.format("%02d", h));
        cmbJam.setValue("12");
        cmbJam.setStyle("-fx-font-size: 12px; -fx-pref-width: 68px;");

        Label lblSep = new Label(":");
        lblSep.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblSep.setTextFill(Color.web(CLR_DARK));

        cmbMenit = new ComboBox<>();
        cmbMenit.getItems().addAll("00", "15", "30", "45");
        cmbMenit.setValue("00");
        cmbMenit.setStyle("-fx-font-size: 12px; -fx-pref-width: 68px;");

        timeBox.getChildren().addAll(cmbJam, lblSep, cmbMenit);

        btnSimpan = new Button("📌 Booking Meja!");
        btnSimpan.setMaxWidth(Double.MAX_VALUE);
        String simpanNormal = "-fx-background-color: " + CLR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-font-size: 12px; -fx-background-radius: 8;";
        String simpanHover  = "-fx-background-color: #A93226; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-font-size: 12px; -fx-background-radius: 8; -fx-cursor: hand;";
        btnSimpan.setStyle(simpanNormal);
        btnSimpan.setOnMouseEntered(e -> btnSimpan.setStyle(simpanHover));
        btnSimpan.setOnMouseExited(e -> btnSimpan.setStyle(simpanNormal));

        formCard.getChildren().addAll(
                lblTitle, line,
                makeLabel("Nama Pelanggan:"), txtNama,
                makeLabel("Meja Dipilih:"), txtMeja,
                makeLabel("Tanggal:"), dpTanggal,
                makeLabel("Waktu:"), timeBox,
                btnSimpan);

        return formCard;
    }

    private VBox buildManageTableCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(12));
        card.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        card.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);" +
                "-fx-border-color: " + CLR_BORDER + ";" +
                "-fx-border-radius: 12;");

        Label lblTitle = new Label("⚙️ Kelola Meja");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTitle.setTextFill(Color.web(CLR_DARK));

        txtCustomMeja = new TextField();
        txtCustomMeja.setPromptText("Nomor meja baru (mis: 13)");
        txtCustomMeja.setStyle(
                "-fx-font-size: 12px; -fx-background-radius: 7; -fx-border-radius: 7;" +
                "-fx-border-color: " + CLR_BORDER + "; -fx-padding: 7;");
        txtCustomMeja.setMaxWidth(Double.MAX_VALUE);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);

        btnTambahMeja = new Button("➕ Tambah");
        btnTambahMeja.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnTambahMeja, Priority.ALWAYS);
        String tambahNormal = "-fx-background-color: #EAFAF1; -fx-text-fill: #1E8449; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 8; -fx-border-color: #A9DFBF; -fx-border-radius: 8;";
        String tambahHover  = "-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        btnTambahMeja.setStyle(tambahNormal);
        btnTambahMeja.setOnMouseEntered(e -> btnTambahMeja.setStyle(tambahHover));
        btnTambahMeja.setOnMouseExited(e -> btnTambahMeja.setStyle(tambahNormal));

        btnHapusMeja = new Button("🗑️ Hapus");
        btnHapusMeja.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnHapusMeja, Priority.ALWAYS);
        String hapusNormal = "-fx-background-color: #FDEDEC; -fx-text-fill: " + CLR_PRIMARY + "; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 8; -fx-border-color: #F5B7B1; -fx-border-radius: 8;";
        String hapusHover  = "-fx-background-color: " + CLR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-background-radius: 8; -fx-cursor: hand;";
        btnHapusMeja.setStyle(hapusNormal);
        btnHapusMeja.setOnMouseEntered(e -> btnHapusMeja.setStyle(hapusHover));
        btnHapusMeja.setOnMouseExited(e -> btnHapusMeja.setStyle(hapusNormal));

        btnBox.getChildren().addAll(btnTambahMeja, btnHapusMeja);
        card.getChildren().addAll(lblTitle, makeLabel("Nomor Meja:"), txtCustomMeja, btnBox);
        return card;
    }

    private VBox buildVisualArea() {
        VBox visualBox = new VBox(15);
        visualBox.setPadding(new Insets(12));
        visualBox.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        visualBox.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);" +
                "-fx-border-color: " + CLR_BORDER + ";" +
                "-fx-border-radius: 12;");

        // Header area visual
        HBox visualHeader = new HBox(15);
        visualHeader.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("🗺️ Denah Meja (Live 🔴)");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTitle.setTextFill(Color.web(CLR_DARK));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Legenda
        HBox legend = new HBox(10);
        legend.setAlignment(Pos.CENTER_RIGHT);
        legend.getChildren().addAll(
                makeLegend("Tersedia", "#27AE60"),
                makeLegend("Terisi",   CLR_PRIMARY));

        visualHeader.getChildren().addAll(lblTitle, spacer, legend);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        tableGrid = new GridPane();
        tableGrid.setHgap(12);
        tableGrid.setVgap(12);
        tableGrid.setAlignment(Pos.TOP_CENTER);
        tableGrid.setPadding(new Insets(10));

        scrollPane.setContent(tableGrid);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        visualBox.getChildren().addAll(visualHeader, scrollPane);
        return visualBox;
    }

    /** Buat satu item legenda warna */
    private HBox makeLegend(String text, String color) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER);

        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(12, 12);
        rect.setFill(Color.web(color));
        rect.setArcWidth(4);
        rect.setArcHeight(4);

        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", 11));
        lbl.setTextFill(Color.web("#666666"));

        box.getChildren().addAll(rect, lbl);
        return box;
    }

    private Label makeLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web("#555555"));
        return lbl;
    }

    // ── Getter ──
    public BorderPane getView()                { return root; }
    public TextField getTxtNama()              { return txtNama; }
    public TextField getTxtMeja()              { return txtMeja; }
    public DatePicker getDpTanggal()           { return dpTanggal; }
    public ComboBox<String> getCmbJam()        { return cmbJam; }
    public ComboBox<String> getCmbMenit()      { return cmbMenit; }
    public Button getBtnSimpan()               { return btnSimpan; }
    public TextField getTxtCustomMeja()        { return txtCustomMeja; }
    public Button getBtnTambahMeja()           { return btnTambahMeja; }
    public Button getBtnHapusMeja()            { return btnHapusMeja; }
    public GridPane getTableGrid()             { return tableGrid; }
    public ReservationController getController() { return controller; }
}
