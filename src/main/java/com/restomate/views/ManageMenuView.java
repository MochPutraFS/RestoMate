package com.restomate.views;

import com.restomate.controllers.ManageMenuController;
import com.restomate.models.MenuRestoran;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;

public class ManageMenuView {

    private BorderPane root;
    private ManageMenuController controller;

    private TableView<MenuRestoran> tableMenu;
    private TextField txtSearch;
    private ComboBox<String> cmbFilter;
    private Button btnDelete;

    private TextField txtId, txtNama, txtHarga, txtStok, txtTingkatPedas;
    private ComboBox<String> cmbKategori;
    private CheckBox chkDingin;
    private Button btnSave, btnClear;

    private Label lblTotalMenu, lblTotalMakanan, lblTotalMinuman, lblTotalKritis;
    private Button btnExportCsv, btnImportCsv, btnDuplicate;
    private Label lblPedas;

    // ── Warna Tema Restoran ──
    private static final String CLR_PRIMARY = "#C0392B";
    private static final String CLR_BG      = "#FDF6EC";
    private static final String CLR_BORDER  = "#E8DDD0";
    private static final String CLR_DARK    = "#2C1A0E";

    public ManageMenuView() {
        root = new BorderPane();
        root.setPadding(new Insets(0));
        root.setBackground(new Background(new BackgroundFill(
                Color.web(CLR_BG), CornerRadii.EMPTY, Insets.EMPTY)));

        // Header
        root.setTop(buildHeader());

        // Tengah: List menu
        VBox centerArea = buildCenterArea();
        BorderPane.setMargin(centerArea, new Insets(0, 0, 0, 15));
        root.setCenter(centerArea);

        // Kanan: Form input
        VBox rightForm = buildFormArea();
        ScrollPane scrollPane = new ScrollPane(rightForm);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background-color: white; " +
                "-fx-background: white; " +
                "-fx-border-color: " + CLR_BORDER + "; " +
                "-fx-border-width: 0 0 0 1;");
        scrollPane.setPrefWidth(310);
        root.setRight(scrollPane);

        controller = new ManageMenuController(this);
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setStyle("-fx-border-color: " + CLR_BORDER + "; -fx-border-width: 0 0 1 0;");

        Label lblTitle = new Label("📋 Kelola Menu Restoran");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblTitle.setTextFill(Color.web(CLR_DARK));

        header.getChildren().add(lblTitle);
        return header;
    }

    private VBox buildCenterArea() {
        VBox centerBox = new VBox(12);
        centerBox.setPadding(new Insets(10, 0, 10, 0));

        // Stats bar
        HBox statsBar = buildStatsBar();

        // Top bar: Search & Filter
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 15, 10, 15));
        topBar.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        topBar.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Cari nama menu...");
        txtSearch.setPrefWidth(230);
        txtSearch.setStyle(
                "-fx-font-size: 12px; -fx-padding: 8px;" +
                "-fx-background-radius: 8; -fx-border-radius: 8;" +
                "-fx-border-color: " + CLR_BORDER + ";");

        cmbFilter = new ComboBox<>();
        cmbFilter.getItems().addAll("SEMUA", "MAKANAN", "MINUMAN");
        cmbFilter.setValue("SEMUA");
        cmbFilter.setStyle("-fx-font-size: 12px; -fx-pref-height: 36px; -fx-background-radius: 8;");

        Label lblCari = new Label("Cari:");
        lblCari.setTextFill(Color.web("#666666"));
        Label lblKat = new Label("Kategori:");
        lblKat.setTextFill(Color.web("#666666"));

        topBar.getChildren().addAll(lblCari, txtSearch, lblKat, cmbFilter);

        // Tabel Menu
        tableMenu = new TableView<>();
        tableMenu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableMenu.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        VBox.setVgrow(tableMenu, Priority.ALWAYS);

        // Bottom bar: Tombol aksi
        HBox bottomBar = new HBox(10);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(5, 0, 0, 0));

        btnImportCsv = createSecondaryButton("📥 Impor CSV");
        btnExportCsv = createSecondaryButton("📤 Ekspor CSV");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnDelete = new Button("🗑️ Hapus Terpilih");
        String delNormal = "-fx-background-color: #FDEDEC; -fx-text-fill: #C0392B; -fx-font-weight: bold; -fx-padding: 8 14; -fx-background-radius: 8;";
        String delHover  = "-fx-background-color: #C0392B; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;";
        btnDelete.setStyle(delNormal);
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(delHover));
        btnDelete.setOnMouseExited(e -> btnDelete.setStyle(delNormal));

        bottomBar.getChildren().addAll(btnImportCsv, btnExportCsv, spacer, btnDelete);

        centerBox.getChildren().addAll(statsBar, topBar, tableMenu, bottomBar);
        return centerBox;
    }

    private HBox buildStatsBar() {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(0, 0, 3, 0));
        bar.setAlignment(Pos.CENTER);

        // Kartu stats bergaya restoran
        VBox cardTotal    = createStatsCard("📦 TOTAL MENU",  lblTotalMenu    = new Label("0"), "#FDF6EC", CLR_DARK,    "#F39C12");
        VBox cardMakanan  = createStatsCard("🍛 MAKANAN",     lblTotalMakanan = new Label("0"), "#FEF9E7", "#7D6608",   "#F1C40F");
        VBox cardMinuman  = createStatsCard("🥤 MINUMAN",     lblTotalMinuman = new Label("0"), "#EBF5FB", "#1A5276",   "#2980B9");
        VBox cardKritis   = createStatsCard("⚠️ STOK KRITIS", lblTotalKritis  = new Label("0"), "#FDEDEC", CLR_PRIMARY, "#E74C3C");

        HBox.setHgrow(cardTotal,   Priority.ALWAYS);
        HBox.setHgrow(cardMakanan, Priority.ALWAYS);
        HBox.setHgrow(cardMinuman, Priority.ALWAYS);
        HBox.setHgrow(cardKritis,  Priority.ALWAYS);

        bar.getChildren().addAll(cardTotal, cardMakanan, cardMinuman, cardKritis);
        return bar;
    }

    private VBox createStatsCard(String title, Label lblValue, String bgHex, String textHex, String accentHex) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(6, 8, 6, 8));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setBackground(new Background(new BackgroundFill(
                Color.web(bgHex), new CornerRadii(10), Insets.EMPTY)));
        card.setStyle(
                "-fx-border-color: " + accentHex + ";" +
                "-fx-border-width: 0 0 0 4;" +
                "-fx-border-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 6, 0, 0, 2);");

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        lblTitle.setTextFill(Color.web(textHex));

        lblValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblValue.setTextFill(Color.web(accentHex));

        card.getChildren().addAll(lblTitle, lblValue);
        return card;
    }

    private VBox buildFormArea() {
        VBox formCard = new VBox(18);
        formCard.setPadding(new Insets(12, 14, 12, 14));
        formCard.setPrefWidth(290);
        formCard.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        BorderPane.setMargin(formCard, new Insets(0));

        Label lblTitle = new Label("✏️ Detail Menu");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblTitle.setTextFill(Color.web(CLR_DARK));

        // Garis dekoratif bawah judul
        javafx.scene.shape.Rectangle line = new javafx.scene.shape.Rectangle(50, 3);
        line.setFill(Color.web(CLR_PRIMARY));

        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(12);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(90);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        String fieldStyle =
                "-fx-font-size: 12px; -fx-background-radius: 7; -fx-border-radius: 7;" +
                "-fx-border-color: " + CLR_BORDER + "; -fx-padding: 7;";

        txtId = new TextField();
        txtId.setDisable(true);
        txtId.setPromptText("Otomatis");
        txtId.setStyle(fieldStyle + "-fx-background-color: #F8F0E8;");
        txtId.setMaxWidth(Double.MAX_VALUE);

        txtNama = new TextField();
        txtNama.setStyle(fieldStyle);
        txtNama.setMaxWidth(Double.MAX_VALUE);

        txtHarga = new TextField();
        txtHarga.setPromptText("Contoh: 15000");
        txtHarga.setStyle(fieldStyle);
        txtHarga.setMaxWidth(Double.MAX_VALUE);

        txtStok = new TextField();
        txtStok.setPromptText("Contoh: 50");
        txtStok.setStyle(fieldStyle);
        txtStok.setMaxWidth(Double.MAX_VALUE);

        cmbKategori = new ComboBox<>();
        cmbKategori.getItems().addAll("MAKANAN", "MINUMAN");
        cmbKategori.setValue("MAKANAN");
        cmbKategori.setMaxWidth(Double.MAX_VALUE);
        cmbKategori.setStyle("-fx-font-size: 12px;");

        txtTingkatPedas = new TextField();
        txtTingkatPedas.setPromptText("Contoh: Level 3");
        txtTingkatPedas.setStyle(fieldStyle);
        txtTingkatPedas.setMaxWidth(Double.MAX_VALUE);

        chkDingin = new CheckBox("Disajikan dengan Es (Dingin)");
        chkDingin.setFont(Font.font("Segoe UI", 12));
        chkDingin.setVisible(false);
        chkDingin.setManaged(false);

        lblPedas = new Label("Lv. Pedas:");
        lblPedas.setFont(Font.font("Segoe UI", 12));
        lblPedas.setTextFill(Color.web("#555555"));

        Label l1 = makeLabel("ID Menu:");
        Label l2 = makeLabel("Nama:");
        Label l3 = makeLabel("Kategori:");
        Label l4 = makeLabel("Harga (Rp):");
        Label l5 = makeLabel("Stok:");

        grid.add(l1, 0, 0); grid.add(txtId, 1, 0);
        grid.add(l2, 0, 1); grid.add(txtNama, 1, 1);
        grid.add(l3, 0, 2); grid.add(cmbKategori, 1, 2);
        grid.add(l4, 0, 3); grid.add(txtHarga, 1, 3);
        grid.add(l5, 0, 4); grid.add(txtStok, 1, 4);
        grid.add(lblPedas, 0, 5); grid.add(txtTingkatPedas, 1, 5);
        grid.add(chkDingin, 1, 6);

        cmbKategori.setOnAction(e -> {
            boolean isMakanan = "MAKANAN".equals(cmbKategori.getValue());
            lblPedas.setVisible(isMakanan);
            lblPedas.setManaged(isMakanan);
            txtTingkatPedas.setVisible(isMakanan);
            txtTingkatPedas.setManaged(isMakanan);
            chkDingin.setVisible(!isMakanan);
            chkDingin.setManaged(!isMakanan);
        });

        // Tombol aksi
        VBox actionBox = new VBox(8);

        btnSave = new Button("💾 Simpan Data");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        String saveNormal = "-fx-background-color: " + CLR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-font-size: 12px; -fx-background-radius: 8;";
        String saveHover  = "-fx-background-color: #A93226; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-font-size: 12px; -fx-background-radius: 8; -fx-cursor: hand;";
        btnSave.setStyle(saveNormal);
        btnSave.setOnMouseEntered(e -> btnSave.setStyle(saveHover));
        btnSave.setOnMouseExited(e -> btnSave.setStyle(saveNormal));

        btnDuplicate = new Button("👥 Duplikat Terpilih");
        btnDuplicate.setMaxWidth(Double.MAX_VALUE);
        String dupNormal = "-fx-background-color: #EBF5FB; -fx-text-fill: #1A5276; -fx-font-weight: bold; -fx-padding: 8; -fx-font-size: 12px; -fx-background-radius: 8;";
        String dupHover  = "-fx-background-color: #2980B9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-font-size: 12px; -fx-background-radius: 8; -fx-cursor: hand;";
        btnDuplicate.setStyle(dupNormal);
        btnDuplicate.setOnMouseEntered(e -> btnDuplicate.setStyle(dupHover));
        btnDuplicate.setOnMouseExited(e -> btnDuplicate.setStyle(dupNormal));

        btnClear = new Button("✨ Bersihkan Form");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        String clrNormal = "-fx-background-color: #F8F0E8; -fx-text-fill: #555; -fx-font-weight: bold; -fx-padding: 8; -fx-font-size: 12px; -fx-background-radius: 8; -fx-border-color: " + CLR_BORDER + "; -fx-border-radius: 8;";
        String clrHover  = "-fx-background-color: #E8DDD0; -fx-text-fill: #333; -fx-font-weight: bold; -fx-padding: 8; -fx-font-size: 12px; -fx-background-radius: 8; -fx-cursor: hand;";
        btnClear.setStyle(clrNormal);
        btnClear.setOnMouseEntered(e -> btnClear.setStyle(clrHover));
        btnClear.setOnMouseExited(e -> btnClear.setStyle(clrNormal));

        actionBox.getChildren().addAll(btnSave, btnDuplicate, btnClear);

        formCard.getChildren().addAll(lblTitle, line, grid, actionBox);
        return formCard;
    }

    private Label makeLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", 12));
        lbl.setTextFill(Color.web("#555555"));
        return lbl;
    }

    private Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        String normal = "-fx-background-color: #F8F0E8; -fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 8; -fx-border-color: " + CLR_BORDER + "; -fx-border-radius: 8;";
        String hover  = "-fx-background-color: #E8DDD0; -fx-text-fill: #2C1A0E; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-radius: 8; -fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    // ── Getter ──
    public BorderPane getView()                       { return root; }
    public TableView<MenuRestoran> getTableMenu()     { return tableMenu; }
    public TextField getTxtSearch()                   { return txtSearch; }
    public ComboBox<String> getCmbFilter()            { return cmbFilter; }
    public Button getBtnDelete()                      { return btnDelete; }
    public TextField getTxtId()                       { return txtId; }
    public TextField getTxtNama()                     { return txtNama; }
    public TextField getTxtHarga()                    { return txtHarga; }
    public TextField getTxtStok()                     { return txtStok; }
    public TextField getTxtTingkatPedas()             { return txtTingkatPedas; }
    public ComboBox<String> getCmbKategori()          { return cmbKategori; }
    public CheckBox getChkDingin()                    { return chkDingin; }
    public Button getBtnSave()                        { return btnSave; }
    public Button getBtnClear()                       { return btnClear; }
    public Label getLblTotalMenu()                    { return lblTotalMenu; }
    public Label getLblTotalMakanan()                 { return lblTotalMakanan; }
    public Label getLblTotalMinuman()                 { return lblTotalMinuman; }
    public Label getLblTotalKritis()                  { return lblTotalKritis; }
    public Button getBtnExportCsv()                   { return btnExportCsv; }
    public Button getBtnImportCsv()                   { return btnImportCsv; }
    public Button getBtnDuplicate()                   { return btnDuplicate; }
    public ManageMenuController getController()       { return controller; }
}
