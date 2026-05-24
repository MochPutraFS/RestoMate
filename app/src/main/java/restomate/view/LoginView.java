package restomate.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import restomate.controller.LoginController;

public class LoginView {
    private Scene scene;
    private LoginController controller;
    private Stage primaryStage;

    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.controller = new LoginController();
        createView();
    }

    private void createView() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #F4F7FC;");

        Label titleLabel = new Label("RestoMate Login");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2C3E50;");

        VBox formBox = new VBox(15);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(30));
        formBox.setMaxWidth(400);
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px;");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 5;");
        loginButton.setPrefWidth(200);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (controller.authenticate(username, password)) {
                errorLabel.setText("");
                System.out.println("Login Sukses! Beralih ke Dashboard...");
                DashboardView dashboardView = new DashboardView(primaryStage);
                primaryStage.setScene(dashboardView.getScene());
            } else {
                errorLabel.setText("Username atau password salah!");
            }
        });

        formBox.getChildren().addAll(usernameField, passwordField, loginButton, errorLabel);
        root.getChildren().addAll(titleLabel, formBox);

        scene = new Scene(root, 800, 600);
    }

    public Scene getScene() {
        return scene;
    }
}