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

public class DashboardView {

    private BorderPane root;
    private StackPane centerContainer;
    private User currentUser;
    
    // Simpan instance view biar gak bolak-balik dibikin ulang pas diklik (menghemat memori)
    private CashierView cashierView;
    private ManageMenuView manageMenuView;
    private ReservationView reservationView;
    private ReportView reportView;
    
    // Pelacakan status aktif sidebar
    private Button activeButton;
    private Button btnKasirRef;

    private static final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-alignment: center-left; -fx-padding: 12 20; -fx-font-weight: normal; -fx-border-color: transparent; -fx-border-width: 0 0 0 4; -fx-border-style: solid; -fx-background-radius: 0; -fx-font-size: 14px;";
    private static final String STYLE_ACTIVE = "-fx-background-color: #E3F2FD; -fx-text-fill: #2196F3; -fx-alignment: center-left; -fx-padding: 12 20; -fx-font-weight: bold; -fx-border-color: #2196F3; -fx-border-width: 0 0 0 4; -fx-border-style: solid; -fx-background-radius: 0; -fx-font-size: 14px;";
    private static final String STYLE_HOVER = "-fx-background-color: #F0F4C3; -fx-text-fill: #827717; -fx-alignment: center-left; -fx-padding: 12 20; -fx-font-weight: normal; -fx-border-color: #D4E157; -fx-border-width: 0 0 0 4; -fx-border-style: solid; -fx-background-radius: 0; -fx-font-size: 14px;";
    // Kita gunakan perpaduan warna hover premium yang lebih selaras dengan tema biru RestoMate:
    private static final String STYLE_HOVER_BLUE = "-fx-background-color: #ECEFF1; -fx-text-fill: #2196F3; -fx-alignment: center-left; -fx-padding: 12 20; -fx-font-weight: normal; -fx-border-color: #CFD8DC; -fx-border-width: 0 0 0 4; -fx-border-style: solid; -fx-background-radius: 0; -fx-font-size: 14px;";

    public DashboardView(User currentUser) {
        this.currentUser = currentUser;
        
        // Inisialisasi semua layar di awal
        cashierView = new CashierView();
        manageMenuView = new ManageMenuView();
        reservationView = new ReservationView();
        reportView = new ReportView();
        
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();
        
        // Siapin kontainer tengah
        centerContainer = new StackPane();
        centerContainer.setPadding(new Insets(20));
        centerContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), Insets.EMPTY)));
        BorderPane.setMargin(centerContainer, new Insets(20, 20, 20, 0)); 
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.05));
        shadow.setRadius(10);
        centerContainer.setEffect(shadow);
        
        root.setCenter(centerContainer);
        
        // Siapin sidebar di sebelah kiri
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);
        
        // Langsung arahkan ke halaman Kasir (POS) sebagai default startup screen
        if (btnKasirRef != null) {
            btnKasirRef.fire();
        }
    }
    
    private VBox buildSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 0, 20, 0)); // Padding horizontal dinolkan biar background-color tombol melar penuh
        sidebar.setPrefWidth(220);
        
        // Background abu-abu netral & border pembatas kanan
        sidebar.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 0 1 0 0;");
        
        Label logo = new Label("RestoMate");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        logo.setTextFill(Color.web("#2196F3"));
        VBox.setMargin(logo, new Insets(10, 20, 20, 20)); 
        
        Label userLabel = new Label("Halo, " + currentUser.getUsername() + "!");
        userLabel.setTextFill(Color.GRAY);
        userLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        VBox.setMargin(userLabel, new Insets(0, 20, 20, 20));
        
        Button btnKasir = createSidebarButton("🛒 Kasir (POS)");
        Button btnMenu = createSidebarButton("📋 Kelola Menu");
        Button btnReservasi = createSidebarButton("📅 Reservasi Meja");
        Button btnLaporan = createSidebarButton("📊 Laporan");
        
        // Spacer untuk mendorong tombol logout ke bagian bawah
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button btnLogout = createSidebarButton("🚪 Logout");
        
        // --- LOGIKA NAVIGASI ---
        btnKasir.setOnAction(e -> {
            setActiveButton(btnKasir);
            cashierView.getController().refresh();
            setCenterContent(cashierView.getView());
        });
        btnMenu.setOnAction(e -> {
            setActiveButton(btnMenu);
            manageMenuView.getController().refresh();
            setCenterContent(manageMenuView.getView());
        });
        btnReservasi.setOnAction(e -> {
            setActiveButton(btnReservasi);
            setCenterContent(reservationView.getView());
        });
        btnLaporan.setOnAction(e -> {
            setActiveButton(btnLaporan);
            reportView.getController().refresh();
            setCenterContent(reportView.getView());
        });
        // -----------------------
        
        // Custom Hover khusus Logout (Warna merah transparan)
        btnLogout.setOnMouseEntered(e -> {
            btnLogout.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F; -fx-alignment: center-left; -fx-padding: 12 20; -fx-font-size: 14px; -fx-border-color: #FFCDD2; -fx-border-width: 0 0 0 4; -fx-border-style: solid; -fx-background-radius: 0; -fx-font-weight: bold;");
            btnLogout.setCursor(javafx.scene.Cursor.HAND);
        });
        btnLogout.setOnMouseExited(e -> {
            if (btnLogout != activeButton) {
                btnLogout.setStyle(STYLE_NORMAL);
            }
        });
        
        btnLogout.setOnAction(e -> {
            System.out.println("User " + currentUser.getUsername() + " pamit logout.");
            
            // Hentikan background polling reservasi saat logout
            if (reservationView.getController() != null) {
                reservationView.getController().stopPolling();
            }
            
            Main.setRoot(new LoginView().getView());
        });
        
        sidebar.getChildren().addAll(logo, userLabel, btnKasir, btnMenu, btnReservasi, btnLaporan, spacer, btnLogout);
        
        // Simpan referensi ke tombol kasir
        this.btnKasirRef = btnKasir;
        
        return sidebar;
    }
    
    private void setActiveButton(Button btn) {
        if (activeButton != null) {
            activeButton.setStyle(STYLE_NORMAL);
        }
        activeButton = btn;
        if (activeButton != null) {
            activeButton.setStyle(STYLE_ACTIVE);
        }
    }
    
    private void setCenterContent(Node node) {
        centerContainer.getChildren().clear();
        centerContainer.getChildren().add(node);
    }
    
    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE); 
        btn.setStyle(STYLE_NORMAL);
        btn.setFont(Font.font("Segoe UI", 14));
        
        btn.setOnMouseEntered(e -> {
            if (btn != activeButton) {
                btn.setStyle(STYLE_HOVER_BLUE);
                btn.setCursor(javafx.scene.Cursor.HAND);
            }
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeButton) {
                btn.setStyle(STYLE_NORMAL);
            }
        });
        
        return btn;
    }
    
    public BorderPane getView() {
        return root;
    }
}
