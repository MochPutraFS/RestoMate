package com.restomate.views;

import com.restomate.Main;
import com.restomate.models.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Rectangle;

public class DashboardView {

    private static DashboardView instance;

    public static DashboardView getInstance() {
        return instance;
    }

    private BorderPane root;
    private StackPane centerContainer;
    private User currentUser;

    private CashierView cashierView;
    private ManageMenuView manageMenuView;
    private ReservationView reservationView;
    private ReportView reportView;

    private Button activeButton;
    private Button btnKasirRef;

    // === Tema Restoran: Sidebar coklat tua, aksen merah marun ===
    private static final String COLOR_SIDEBAR   = "#2C1A0E";
    private static final String COLOR_ACTIVE_BG = "#C0392B";
    private static final String COLOR_HOVER_BG  = "#3D2512";
    private static final String COLOR_TEXT_NORM = "#BDC3C7";
    private static final String COLOR_TEXT_ACT  = "#FFFFFF";

    private static final String STYLE_NORMAL =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + COLOR_TEXT_NORM + ";" +
            "-fx-alignment: center-left;" +
            "-fx-padding: 10 15;" +
            "-fx-font-size: 12px;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: transparent;" +
            "-fx-border-width: 0 0 0 4;";

    private static final String STYLE_ACTIVE =
            "-fx-background-color: " + COLOR_ACTIVE_BG + ";" +
            "-fx-text-fill: " + COLOR_TEXT_ACT + ";" +
            "-fx-alignment: center-left;" +
            "-fx-padding: 10 15;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: #F39C12;" +
            "-fx-border-width: 0 0 0 4;";

    private static final String STYLE_HOVER =
            "-fx-background-color: " + COLOR_HOVER_BG + ";" +
            "-fx-text-fill: white;" +
            "-fx-alignment: center-left;" +
            "-fx-padding: 10 15;" +
            "-fx-font-size: 12px;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: #C0392B;" +
            "-fx-border-width: 0 0 0 4;";

    public DashboardView(User currentUser) {
        instance = this;
        this.currentUser = currentUser;
        cashierView     = new CashierView();
        manageMenuView  = new ManageMenuView();
        reservationView = new ReservationView();
        reportView      = new ReportView();
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
                Color.web("#FDF6EC"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Area tengah konten
        centerContainer = new StackPane();
        centerContainer.setPadding(new Insets(20));
        centerContainer.setBackground(new Background(new BackgroundFill(
                Color.web("#FDF6EC"), CornerRadii.EMPTY, Insets.EMPTY)));
        BorderPane.setMargin(centerContainer, new Insets(0, 15, 15, 0));

        root.setCenter(centerContainer);
        root.setLeft(buildSidebar());

        if (btnKasirRef != null) {
            btnKasirRef.fire();
        }
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPadding(new Insets(0));
        sidebar.setPrefWidth(190);
        sidebar.setBackground(new Background(new BackgroundFill(
                Color.web(COLOR_SIDEBAR), CornerRadii.EMPTY, Insets.EMPTY)));

        // ── Header Logo ──
        VBox logoBox = new VBox(5);
        logoBox.setPadding(new Insets(16, 14, 14, 14));
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Label lblIcon = new Label("🍽️");
        lblIcon.setFont(Font.font("Segoe UI", 22));

        Label logo = new Label("RestoMate");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        logo.setTextFill(Color.web("#F39C12"));

        // Garis bawah logo
        Rectangle logoLine = new Rectangle(185, 1);
        logoLine.setFill(Color.web("#3D2512"));

        logoBox.getChildren().addAll(lblIcon, logo, logoLine);

        // ── Info User ──
        VBox userBox = new VBox(3);
        userBox.setPadding(new Insets(8, 14, 12, 14));

        Label lblHalo = new Label("Halo,");
        lblHalo.setFont(Font.font("Segoe UI", 11));
        lblHalo.setTextFill(Color.web("#7F8C8D"));

        Label lblUsername = new Label(currentUser.getUsername());
        lblUsername.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblUsername.setTextFill(Color.web("#ECF0F1"));

        Label lblRole = new Label("● Shift Aktif");
        lblRole.setFont(Font.font("Segoe UI", 11));
        lblRole.setTextFill(Color.web("#27AE60"));

        userBox.getChildren().addAll(lblHalo, lblUsername, lblRole);

        // ── Tombol Navigasi ──
        Button btnKasir     = createSidebarButton("🛒  Kasir (POS)");
        Button btnMenu      = createSidebarButton("📋  Kelola Menu");
        Button btnReservasi = createSidebarButton("📅  Reservasi Meja");
        Button btnLaporan   = createSidebarButton("📊  Laporan");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ── Tombol Logout ──
        Button btnLogout = createSidebarButton("🚪  Logout");

        String logoutHover =
                "-fx-background-color: #922B21;" +
                "-fx-text-fill: white;" +
                "-fx-alignment: center-left;" +
                "-fx-padding: 10 15;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 0;" +
                "-fx-border-color: #E74C3C;" +
                "-fx-border-width: 0 0 0 4;";

        btnLogout.setOnMouseEntered(e -> {
            if (btnLogout != activeButton) btnLogout.setStyle(logoutHover);
            btnLogout.setCursor(javafx.scene.Cursor.HAND);
        });
        btnLogout.setOnMouseExited(e -> {
            if (btnLogout != activeButton) btnLogout.setStyle(STYLE_NORMAL);
        });

        // ── Aksi Navigasi ──
        btnKasir.setOnAction(e -> {
            if (reservationView.getController() != null) {
                reservationView.getController().stopPolling();
            }
            setActiveButton(btnKasir);
            cashierView.getController().refresh();
            setCenterContent(cashierView.getView());
        });
        btnMenu.setOnAction(e -> {
            if (reservationView.getController() != null) {
                reservationView.getController().stopPolling();
            }
            setActiveButton(btnMenu);
            manageMenuView.getController().refresh();
            setCenterContent(manageMenuView.getView());
        });
        btnReservasi.setOnAction(e -> {
            setActiveButton(btnReservasi);
            setCenterContent(reservationView.getView());
            if (reservationView.getController() != null) {
                reservationView.getController().startPolling();
            }
        });
        btnLaporan.setOnAction(e -> {
            if (reservationView.getController() != null) {
                reservationView.getController().stopPolling();
            }
            setActiveButton(btnLaporan);
            reportView.getController().refresh();
            setCenterContent(reportView.getView());
        });
        btnLogout.setOnAction(e -> {
            if (reservationView.getController() != null) {
                reservationView.getController().stopPolling();
            }
            Main.setRoot(new LoginView().getView());
        });

        this.btnKasirRef = btnKasir;

        sidebar.getChildren().addAll(
                logoBox, userBox,
                btnKasir, btnMenu, btnReservasi, btnLaporan,
                spacer, btnLogout);

        return sidebar;
    }

    private void setActiveButton(Button btn) {
        if (activeButton != null) activeButton.setStyle(STYLE_NORMAL);
        activeButton = btn;
        if (activeButton != null) activeButton.setStyle(STYLE_ACTIVE);
    }

    private void setCenterContent(Node node) {
        centerContainer.getChildren().setAll(node);
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(STYLE_NORMAL);
        btn.setFont(Font.font("Segoe UI", 14));
        btn.setOnMouseEntered(e -> {
            if (btn != activeButton) {
                btn.setStyle(STYLE_HOVER);
                btn.setCursor(javafx.scene.Cursor.HAND);
            }
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeButton) btn.setStyle(STYLE_NORMAL);
        });
        return btn;
    }

    public void switchToCashierAndLoadPreOrder(int reservationId, String customerName, String menuDipesan, double dpPaid) {
        if (btnKasirRef != null) {
            cashierView.getController().loadPreOrderReservation(reservationId, customerName, menuDipesan, dpPaid);
            btnKasirRef.fire();
        }
    }

    public BorderPane getView() {
        return root;
    }
}
