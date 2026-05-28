package com.restomate.views;

import com.restomate.controllers.ReservationController;
import com.restomate.models.Reservation;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ReservationView {

    private BorderPane root;
    private ReservationController controller;

    private TextField txtCustomMeja;
    private TextField txtCustomKapasitas;
    private Button btnTambahMeja, btnHapusMeja;

    private GridPane tableGrid;

    // Time Travel & Tab Pane Controls
    private TabPane tabPane;
    private Tab tabDenah;
    private Tab tabDaftar;
    
    private CheckBox chkLiveStatus;
    private DatePicker dpFilterTanggal;
    private ComboBox<String> cmbFilterJam, cmbFilterMenit;
    private HBox filterWaktuBox;

    private TableView<Reservation> tblReservasi;
    private TextField txtSearchDaftar;

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

        // Kiri: Kelola meja
        VBox leftPane = new VBox(15);
        leftPane.setPadding(new Insets(10, 0, 10, 10));
        leftPane.setPrefWidth(260);
        leftPane.getChildren().addAll(buildManageTableCard());
        root.setLeft(leftPane);

        // Tengah: TabPane (Denah & Daftar)
        TabPane centralPane = buildCentralTabPane();
        BorderPane.setMargin(centralPane, new Insets(10, 10, 10, 8));
        root.setCenter(centralPane);

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

        txtCustomKapasitas = new TextField();
        txtCustomKapasitas.setPromptText("Kapasitas meja (mis: 4)");
        txtCustomKapasitas.setStyle(
                "-fx-font-size: 12px; -fx-background-radius: 7; -fx-border-radius: 7;" +
                "-fx-border-color: " + CLR_BORDER + "; -fx-padding: 7;");
        txtCustomKapasitas.setMaxWidth(Double.MAX_VALUE);

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
        card.getChildren().addAll(lblTitle, makeLabel("Nomor Meja:"), txtCustomMeja, makeLabel("Kapasitas Meja:"), txtCustomKapasitas, btnBox);
        return card;
    }

    private TabPane buildCentralTabPane() {
        tabPane = new TabPane();
        tabPane.setStyle("-fx-tab-channel-color: transparent;");
        
        tabDenah = new Tab("🗺️ Denah Meja", buildDenahLayout());
        tabDenah.setClosable(false);
        
        tabDaftar = new Tab("📋 Daftar Booking", buildDaftarLayout());
        tabDaftar.setClosable(false);
        
        tabPane.getTabs().addAll(tabDenah, tabDaftar);
        return tabPane;
    }

    private VBox buildDenahLayout() {
        VBox visualBox = new VBox(15);
        visualBox.setPadding(new Insets(12));
        visualBox.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        visualBox.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);" +
                "-fx-border-color: " + CLR_BORDER + ";" +
                "-fx-border-radius: 12;");

        // Header area visual
        HBox visualHeader = new HBox(12);
        visualHeader.setAlignment(Pos.CENTER_LEFT);

        chkLiveStatus = new CheckBox("Status Live 🔴");
        chkLiveStatus.setSelected(true);
        chkLiveStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        chkLiveStatus.setTextFill(Color.web(CLR_PRIMARY));

        // Time filter controls
        dpFilterTanggal = new DatePicker(LocalDate.now());
        dpFilterTanggal.setStyle("-fx-font-size: 11px; -fx-pref-width: 115px;");

        cmbFilterJam = new ComboBox<>();
        for (int h = 8; h <= 22; h++) cmbFilterJam.getItems().add(String.format("%02d", h));
        cmbFilterJam.setValue("12");
        cmbFilterJam.setStyle("-fx-font-size: 11px; -fx-pref-width: 55px;");

        Label lblSep = new Label(":");
        lblSep.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        cmbFilterMenit = new ComboBox<>();
        cmbFilterMenit.getItems().addAll("00", "15", "30", "45");
        cmbFilterMenit.setValue("00");
        cmbFilterMenit.setStyle("-fx-font-size: 11px; -fx-pref-width: 55px;");

        filterWaktuBox = new HBox(5, dpFilterTanggal, cmbFilterJam, lblSep, cmbFilterMenit);
        filterWaktuBox.setAlignment(Pos.CENTER_LEFT);
        filterWaktuBox.setVisible(false);
        filterWaktuBox.setManaged(false);

        chkLiveStatus.selectedProperty().addListener((obs, oldVal, newVal) -> {
            filterWaktuBox.setVisible(!newVal);
            filterWaktuBox.setManaged(!newVal);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Legenda
        HBox legend = new HBox(10);
        legend.setAlignment(Pos.CENTER_RIGHT);
        legend.getChildren().addAll(
                makeLegend("Tersedia", "#27AE60"),
                makeLegend("Terisi",   CLR_PRIMARY));

        visualHeader.getChildren().addAll(chkLiveStatus, filterWaktuBox, spacer, legend);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        tableGrid = new GridPane();
        tableGrid.setHgap(15);
        tableGrid.setVgap(15);
        tableGrid.setAlignment(Pos.TOP_CENTER);
        tableGrid.setPadding(new Insets(15));

        scrollPane.setContent(tableGrid);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        visualBox.getChildren().addAll(visualHeader, scrollPane);
        return visualBox;
    }

    private VBox buildDaftarLayout() {
        VBox listLayout = new VBox(10);
        listLayout.setPadding(new Insets(12));
        listLayout.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(12), Insets.EMPTY)));
        listLayout.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);" +
                "-fx-border-color: " + CLR_BORDER + ";" +
                "-fx-border-radius: 12;");

        // Search bar
        HBox searchBar = new HBox(8);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        txtSearchDaftar = new TextField();
        txtSearchDaftar.setPromptText("Cari nama pelanggan atau nomor meja...");
        txtSearchDaftar.setStyle("-fx-font-size: 12px; -fx-background-radius: 7; -fx-border-radius: 7; -fx-border-color: #E8DDD0; -fx-padding: 6;");
        txtSearchDaftar.setPrefWidth(280);

        Label lblInfo = new Label("Klik baris meja merah pada denah atau pilih dari tabel di bawah.");
        lblInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblInfo.setTextFill(Color.web("#7F8C8D"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchBar.getChildren().addAll(makeLabel("Cari:"), txtSearchDaftar, spacer, lblInfo);

        // TableView
        tblReservasi = new TableView<>();
        tblReservasi.setStyle("-fx-font-size: 11px;");

        TableColumn<Reservation, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colId.setPrefWidth(40);

        TableColumn<Reservation, String> colNama = new TableColumn<>("Pelanggan");
        colNama.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNamaPelanggan()));
        colNama.setPrefWidth(100);

        TableColumn<Reservation, String> colMeja = new TableColumn<>("Meja");
        colMeja.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Meja " + data.getValue().getNomorMeja()));
        colMeja.setPrefWidth(60);

        TableColumn<Reservation, String> colTamu = new TableColumn<>("Tamu");
        colTamu.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getJumlahOrang() + " Orang"));
        colTamu.setPrefWidth(65);

        TableColumn<Reservation, String> colWaktu = new TableColumn<>("Waktu Reservasi");
        colWaktu.setCellValueFactory(data -> {
            if (data.getValue().getWaktuReservasi() != null) {
                return new javafx.beans.property.SimpleStringProperty(data.getValue().getWaktuReservasi().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colWaktu.setPrefWidth(125);

        TableColumn<Reservation, String> colMenu = new TableColumn<>("Menu Pre-Order");
        colMenu.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (data.getValue().getMenuDipesan() != null && !data.getValue().getMenuDipesan().isEmpty()) ? data.getValue().getMenuDipesan() : "-"));
        colMenu.setPrefWidth(140);

        TableColumn<Reservation, String> colReady = new TableColumn<>("Ready Time");
        colReady.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (data.getValue().getWaktuSiap() != null && !data.getValue().getWaktuSiap().isEmpty()) ? data.getValue().getWaktuSiap() : "-"));
        colReady.setPrefWidth(70);

        TableColumn<Reservation, String> colCatatan = new TableColumn<>("Catatan");
        colCatatan.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (data.getValue().getCatatan() != null && !data.getValue().getCatatan().isEmpty()) ? data.getValue().getCatatan() : "-"));
        colCatatan.setPrefWidth(110);

        TableColumn<Reservation, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        colStatus.setPrefWidth(70);

        tblReservasi.getColumns().addAll(colId, colNama, colMeja, colTamu, colWaktu, colMenu, colReady, colCatatan, colStatus);
        VBox.setVgrow(tblReservasi, Priority.ALWAYS);

        listLayout.getChildren().addAll(searchBar, tblReservasi);
        return listLayout;
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
    public BorderPane getView()                          { return root; }
    public TextField getTxtCustomMeja()                  { return txtCustomMeja; }
    public TextField getTxtCustomKapasitas()             { return txtCustomKapasitas; }
    public Button getBtnTambahMeja()                     { return btnTambahMeja; }
    public Button getBtnHapusMeja()                      { return btnHapusMeja; }
    public GridPane getTableGrid()                       { return tableGrid; }
    
    public TabPane getTabPane()                          { return tabPane; }
    public Tab getTabDenah()                             { return tabDenah; }
    public Tab getTabDaftar()                            { return tabDaftar; }
    
    public CheckBox getChkLiveStatus()                   { return chkLiveStatus; }
    public DatePicker getDpFilterTanggal()               { return dpFilterTanggal; }
    public ComboBox<String> getCmbFilterJam()            { return cmbFilterJam; }
    public ComboBox<String> getCmbFilterMenit()          { return cmbFilterMenit; }
    public HBox getFilterWaktuBox()                      { return filterWaktuBox; }
    
    public TableView<Reservation> getTblReservasi()      { return tblReservasi; }
    public TextField getTxtSearchDaftar()                { return txtSearchDaftar; }
    
    public ReservationController getController()         { return controller; }
}
