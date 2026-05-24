package restomate.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DashboardView {
    private Scene scene;
    private Stage primaryStage;
    private BorderPane root;

    public DashboardView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }

    private ReservationView currentReservationView;

    private void switchView(VBox newView) {
        if (currentReservationView != null) {
            currentReservationView.stopPolling();
            currentReservationView = null;
        }
        root.setCenter(newView);
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F7FC;");

        // Sidebar
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #2C3E50;");
        sidebar.setPrefWidth(250);

        Label titleLabel = new Label("RestoMate");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: white;");
        titleLabel.setPadding(new Insets(0, 0, 30, 0));

        Button btnCashier = createSidebarButton("Menu Kasir");
        Button btnFood = createSidebarButton("Kelola Makanan");
        Button btnReservation = createSidebarButton("Catat Reservasi");
        Button btnReport = createSidebarButton("Laporan Harian");
        
        // Spacer box to push Logout button to bottom
        VBox spacer = new VBox();
        javafx.scene.layout.VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button btnLogout = new Button("Logout");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5;");
        
        sidebar.getChildren().addAll(titleLabel, btnCashier, btnFood, btnReservation, btnReport, spacer, btnLogout);
        root.setLeft(sidebar);

        // Default Center View
        showWelcomeView();

        // Event Listeners for Navigation
        btnCashier.setOnAction(e -> switchView(new CashierView().getView()));
        btnFood.setOnAction(e -> switchView(new ManageFoodView().getView()));
        
        // Fixing the lambda block for Reservation
        btnReservation.setOnAction(e -> {
            switchView(new VBox()); // Clear first to stop previous
            currentReservationView = new ReservationView();
            root.setCenter(currentReservationView.getView());
        });

        btnReport.setOnAction(e -> switchView(new ReportView().getView()));
        
        btnLogout.setOnAction(e -> {
            if (currentReservationView != null) {
                currentReservationView.stopPolling();
                currentReservationView = null;
            }
            logout();
        });

        scene = new Scene(root, 1024, 768);
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: center-left; -fx-padding: 10px 15px;");
        
        // Hover effects
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: center-left; -fx-padding: 10px 15px; -fx-background-radius: 5;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: center-left; -fx-padding: 10px 15px;"));
        
        return btn;
    }

    private void showWelcomeView() {
        VBox welcomeBox = new VBox(20);
        welcomeBox.setAlignment(Pos.CENTER);
        
        Label welcomeLabel = new Label("Selamat datang di Dashboard RestoMate!");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        welcomeLabel.setStyle("-fx-text-fill: #2C3E50;");
        
        Label descLabel = new Label("Pilih menu di sidebar untuk mulai menggunakan aplikasi.");
        descLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7F8C8D;");
        
        welcomeBox.getChildren().addAll(welcomeLabel, descLabel);
        root.setCenter(welcomeBox);
    }

    private void logout() {
        LoginView loginView = new LoginView(primaryStage);
        primaryStage.setScene(loginView.getScene());
    }

    public Scene getScene() {
        return scene;
    }
}