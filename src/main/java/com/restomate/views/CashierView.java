package com.restomate.views;

import com.restomate.controllers.CashierController;
import com.restomate.models.MenuRestoran;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CashierView {

    private BorderPane root;
    private CashierController controller;

    private TableView<MenuRestoran> menuTable;
    private TableView<CashierController.CartItem> cartTable;
    private Label lblTotal;
    private ComboBox<String> cmbPayment;
    private Button btnPay;

    private TextField txtSearch;
    private ComboBox<String> cmbFilter;
    private Button btnClearCart;
    private TextField txtCatatan;

    private ComboBox<String> cmbDiscount;
    private TextField txtAmountPaid;
    private Label lblChange;
    private Label lblSubtotal;
    private Label lblDiscountAmount;
    private Label lblTaxAmount;

    // ── Warna Tema Restoran ──
    private static final String CLR_PRIMARY   = "#C0392B"; // Merah marun
    private static final String CLR_SUCCESS   = "#27AE60"; // Hijau
    private static final String CLR_DANGER    = "#E74C3C"; // Merah bahaya
    private static final String CLR_BG        = "#FDF6EC"; // Krem muda
    private static final String CLR_CARD      = "#FFFFFF";
    private static final String CLR_BORDER    = "#E8DDD0";

    public CashierView() {
        root = new BorderPane();
        root.setPadding(new Insets(0));
        root.setBackground(new Background(new BackgroundFill(
                Color.web(CLR_BG), CornerRadii.EMPTY, Insets.EMPTY)));

        // Header halaman
        HBox header = buildHeader();
        root.setTop(header);

        // Area tengah: search + tabel menu
        VBox centerArea = buildCenterArea();
        root.setCenter(centerArea);

        // Sidebar kanan: keranjang
        VBox rightSidebar = buildCartSidebar();
        root.setRight(rightSidebar);

        controller = new CashierController(this);
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setStyle(
                "-fx-border-color: " + CLR_BORDER + ";" +
                "-fx-border-width: 0 0 1 0;");

        Label lblTitle = new Label("🛒 Kasir / Point of Sale");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTitle.setTextFill(Color.web("#2C1A0E"));

        header.getChildren().add(lblTitle);
        return header;
    }

    private VBox buildCenterArea() {
        VBox centerArea = new VBox(12);
        centerArea.setPadding(new Insets(10, 0, 10, 10));

        // Top bar: Search & Filter
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 10, 8, 10));
        topBar.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        topBar.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Cari nama menu (F3)...");
        txtSearch.setPrefWidth(210);
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
        menuTable = new TableView<>();
        menuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        menuTable.setPlaceholder(new Label("Tidak ada menu ditemukan."));
        menuTable.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        VBox.setVgrow(menuTable, Priority.ALWAYS);

        centerArea.getChildren().addAll(topBar, menuTable);
        return centerArea;
    }

    private VBox buildCartSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(310);
        sidebar.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        sidebar.setStyle(
                "-fx-border-color: " + CLR_BORDER + ";" +
                "-fx-border-width: 0 0 0 1;");
        BorderPane.setMargin(sidebar, new Insets(0));

        // Judul keranjang
        HBox cartHeader = new HBox();
        cartHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label("🧾 Keranjang Pesanan");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblTitle.setTextFill(Color.web("#2C1A0E"));
        cartHeader.getChildren().add(lblTitle);

        // Tabel keranjang
        cartTable = new TableView<>();
        cartTable.setPlaceholder(new Label("Keranjang masih kosong."));
        cartTable.setPrefHeight(200);
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        // Tombol Clear Cart
        btnClearCart = new Button("🧹 Bersihkan Keranjang (ESC)");
        btnClearCart.setMaxWidth(Double.MAX_VALUE);
        String clearNormal =
                "-fx-background-color: #FDEDEC; -fx-text-fill: " + CLR_DANGER + ";" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8;";
        String clearHover =
                "-fx-background-color: " + CLR_DANGER + "; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;";
        btnClearCart.setStyle(clearNormal);
        btnClearCart.setOnMouseEntered(e -> btnClearCart.setStyle(clearHover));
        btnClearCart.setOnMouseExited(e -> btnClearCart.setStyle(clearNormal));

        // Grid diskon & catatan
        GridPane detailsGrid = new GridPane();
        detailsGrid.setVgap(8);
        detailsGrid.setHgap(10);

        Label lblDiscTitle = new Label("Diskon:");
        lblDiscTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblDiscTitle.setTextFill(Color.web("#555555"));

        cmbDiscount = new ComboBox<>();
        cmbDiscount.getItems().addAll("0%", "5%", "10%", "15%", "20%");
        cmbDiscount.setValue("0%");
        cmbDiscount.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(cmbDiscount, Priority.ALWAYS);

        Label lblCatatan = new Label("Catatan:");
        lblCatatan.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblCatatan.setTextFill(Color.web("#555555"));

        txtCatatan = new TextField();
        txtCatatan.setPromptText("📝 Catatan pesanan...");
        txtCatatan.setStyle(
                "-fx-font-size: 12px; -fx-padding: 6px;" +
                "-fx-background-radius: 6; -fx-border-radius: 6;" +
                "-fx-border-color: " + CLR_BORDER + ";");

        detailsGrid.add(lblDiscTitle, 0, 0);
        detailsGrid.add(cmbDiscount,  1, 0);
        detailsGrid.add(lblCatatan,   0, 1);
        detailsGrid.add(txtCatatan,   1, 1);

        // Grid metode pembayaran
        GridPane paymentGrid = new GridPane();
        paymentGrid.setVgap(8);
        paymentGrid.setHgap(10);

        Label lblPayMethod = new Label("Metode:");
        lblPayMethod.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblPayMethod.setTextFill(Color.web("#555555"));

        cmbPayment = new ComboBox<>();
        cmbPayment.getItems().addAll("CASH", "QRIS");
        cmbPayment.setValue("CASH");
        cmbPayment.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(cmbPayment, Priority.ALWAYS);

        Label lblAmountPaid = new Label("Bayar (Rp):");
        lblAmountPaid.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblAmountPaid.setTextFill(Color.web("#555555"));

        txtAmountPaid = new TextField();
        txtAmountPaid.setPromptText("Nominal diterima...");
        txtAmountPaid.setStyle(
                "-fx-font-size: 12px; -fx-padding: 6px;" +
                "-fx-background-radius: 6; -fx-border-radius: 6;" +
                "-fx-border-color: " + CLR_BORDER + ";");
        GridPane.setHgrow(txtAmountPaid, Priority.ALWAYS);

        paymentGrid.add(lblPayMethod,  0, 0);
        paymentGrid.add(cmbPayment,    1, 0);
        paymentGrid.add(lblAmountPaid, 0, 1);
        paymentGrid.add(txtAmountPaid, 1, 1);

        // Rincian biaya
        VBox costBox = new VBox(6);
        costBox.setPadding(new Insets(12));
        costBox.setBackground(new Background(new BackgroundFill(
                Color.web("#FDF6EC"), new CornerRadii(10), Insets.EMPTY)));
        costBox.setStyle("-fx-border-color: " + CLR_BORDER + "; -fx-border-radius: 10;");

        lblSubtotal       = new Label("Rp 0");
        lblDiscountAmount  = new Label("Rp 0");
        lblTaxAmount      = new Label("Rp 0");
        lblTotal          = new Label("Rp 0");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTotal.setTextFill(Color.web(CLR_PRIMARY));
        lblChange         = new Label("Rp 0");
        lblChange.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblChange.setTextFill(Color.web(CLR_SUCCESS));

        costBox.getChildren().addAll(
                buildCostRow("Subtotal:",         lblSubtotal,      false),
                buildCostRow("Diskon:",           lblDiscountAmount, false),
                buildCostRow("Pajak PB1 (10%):",  lblTaxAmount,     false),
                new Separator(),
                buildCostRow("TOTAL:",            lblTotal,          true),
                buildCostRow("Kembalian:",        lblChange,         false));

        // Tombol Bayar
        btnPay = new Button("💳  Proses Pembayaran (F5)");
        btnPay.setMaxWidth(Double.MAX_VALUE);
        String payNormal =
                "-fx-background-color: " + CLR_SUCCESS + "; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-pref-height: 38px; -fx-font-size: 12px;" +
                "-fx-background-radius: 10;";
        String payHover =
                "-fx-background-color: #219150; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-pref-height: 38px; -fx-font-size: 12px;" +
                "-fx-background-radius: 10; -fx-cursor: hand;";
        btnPay.setStyle(payNormal);
        btnPay.setOnMouseEntered(e -> btnPay.setStyle(payHover));
        btnPay.setOnMouseExited(e -> btnPay.setStyle(payNormal));

        sidebar.getChildren().addAll(
                cartHeader, cartTable, btnClearCart,
                new Separator(), detailsGrid,
                new Separator(), paymentGrid,
                costBox, btnPay);

        return sidebar;
    }

    /** Buat satu baris rincian biaya (label + nilai) */
    private HBox buildCostRow(String title, Label valueLabel, boolean bold) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label(title);
        lblTitle.setFont(bold
                ? Font.font("Segoe UI", FontWeight.BOLD, 13)
                : Font.font("Segoe UI", 12));
        lblTitle.setTextFill(Color.web(bold ? "#2C1A0E" : "#666666"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(lblTitle, spacer, valueLabel);
        return row;
    }

    // ── Getter untuk Controller ──
    public BorderPane getView()                              { return root; }
    public TableView<MenuRestoran> getMenuTable()            { return menuTable; }
    public TableView<CashierController.CartItem> getCartTable() { return cartTable; }
    public Label getLblTotal()                               { return lblTotal; }
    public ComboBox<String> getCmbPayment()                  { return cmbPayment; }
    public Button getBtnPay()                                { return btnPay; }
    public TextField getTxtSearch()                          { return txtSearch; }
    public ComboBox<String> getCmbFilter()                   { return cmbFilter; }
    public Button getBtnClearCart()                          { return btnClearCart; }
    public TextField getTxtCatatan()                         { return txtCatatan; }
    public ComboBox<String> getCmbDiscount()                 { return cmbDiscount; }
    public TextField getTxtAmountPaid()                      { return txtAmountPaid; }
    public Label getLblChange()                              { return lblChange; }
    public Label getLblSubtotal()                            { return lblSubtotal; }
    public Label getLblDiscountAmount()                      { return lblDiscountAmount; }
    public Label getLblTaxAmount()                           { return lblTaxAmount; }
    public CashierController getController()                 { return controller; }
}
